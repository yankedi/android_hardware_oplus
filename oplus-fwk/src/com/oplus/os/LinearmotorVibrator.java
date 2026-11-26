/*
 * Copyright (C) 2022 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oplus.os;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.Settings;
import android.util.Slog;

import vendor.oplus.hardware.vibrator.IOplusVibrator;

/**
 * AOSP-backed stand-in for the OPlus {@code com.oplus.os.LinearmotorVibrator} system service.
 *
 * <p>The OPlus camera app talks to the OPlus linear-motor vibrator through
 * {@code Context.getSystemService("linearmotor")}. That service only exists in a stock OPlus
 * framework, so on AOSP the lookup returns {@code null} and all camera haptics are silently
 * dropped.
 *
 * <p>This class is shipped on the boot classpath by {@code oplus-fwk} and provides the same API
 * surface, but drives the standard AOSP {@link Vibrator}/{@link VibratorManager} instead. The
 * camera app is patched (see {@code vendor/oplus/camera/<device>/patches}) to obtain an instance
 * through {@link #create(Context)} rather than the missing OPlus system service.
 *
 * <p>The device's vibrator HAL (vendor.qti.hardware.vibrator) already carries the stock OPlus
 * richtap effect streams, so the predefined AOSP effects used here produce stock-quality haptics.
 */
public class LinearmotorVibrator {
    private static final String TAG = "LinearmotorVibrator";
    private static final String OPLUS_VIBRATOR_SERVICE =
            "vendor.oplus.hardware.vibrator.IOplusVibrator/default";

    private final Context mContext;
    private final Vibrator mVibrator;
    private IOplusVibrator mOplusVibrator;

    public LinearmotorVibrator(Context context, ILinearmotorVibratorService serviceIgnored) {
        mContext = context;

        Vibrator vibrator = null;
        if (context != null) {
            VibratorManager manager =
                    (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (manager != null) {
                vibrator = manager.getDefaultVibrator();
            }
            if (vibrator == null) {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
        }
        mVibrator = vibrator;
    }

    /**
     * Factory used by the patched OPlus camera app in place of
     * {@code Context.getSystemService(Context.LINEARMOTOR_VIBRATOR_SERVICE)}.
     */
    public static LinearmotorVibrator create(Context context) {
        return new LinearmotorVibrator(context, null);
    }

    private synchronized IOplusVibrator getOplusVibrator() {
        if (mOplusVibrator != null) {
            IBinder b = mOplusVibrator.asBinder();
            if (b != null && b.isBinderAlive()) {
                return mOplusVibrator;
            }
            mOplusVibrator = null;
        }
        try {
            IBinder binder = ServiceManager.checkService(OPLUS_VIBRATOR_SERVICE);
            if (binder != null) {
                mOplusVibrator = IOplusVibrator.Stub.asInterface(binder);
            }
        } catch (Throwable t) {
            Slog.w(TAG, "Failed to look up IOplusVibrator", t);
        }
        return mOplusVibrator;
    }

    private boolean shouldAllowStockHaptics() {
        if (mContext == null) {
            return true;
        }
        if ("com.oplus.camera".equals(mContext.getPackageName())) {
            return true;
        }
        try {
            return Settings.System.getInt(
                    mContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0;
        } catch (Throwable t) {
            return true;
        }
    }

    private int mapStrength(int rawStrength) {
        if (rawStrength <= 0) {
            int intensity = 2; // medium default (matches Settings 1/2/3 scale)
            if (mContext != null) {
                try {
                    intensity = Settings.System.getInt(
                            mContext.getContentResolver(), "haptic_feedback_intensity", 2);
                } catch (Throwable ignored) {
                }
            }
            if (intensity <= 1) {
                return 0; // LIGHT
            }
            if (intensity == 2) {
                return 1; // MEDIUM
            }
            return 2; // STRONG
        }
        if (rawStrength <= 800) {
            return 0; // LIGHT
        }
        if (rawStrength <= 1600) {
            return 1; // MEDIUM
        }
        return 2; // STRONG
    }

    private static VibrationEffect toVibrationEffect(WaveformEffect effect) {
        if (effect != null) {
            switch (effect.getEffectType()) {
                case 0:
                case 2:
                case 68:
                case 302:
                case 303:
                case 304:
                case 305:
                    return VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK);
                case 1:
                case 3:
                case 49:
                case 69:
                    return VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK);
                case 4:
                case 5:
                case 51:
                case 52:
                    return VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
                default:
                    break;
            }
        }
        return VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
    }

    public void vibrate(WaveformEffect effect) {
        if (effect != null && shouldAllowStockHaptics()) {
            IOplusVibrator svc = getOplusVibrator();
            if (svc != null) {
                try {
                    int durationMs = svc.playEffect(
                            effect.getEffectType(), mapStrength(effect.getEffectStrength()));
                    if (durationMs >= 0) {
                        return;
                    }
                } catch (RemoteException e) {
                    synchronized (this) {
                        mOplusVibrator = null;
                    }
                } catch (Throwable t) {
                    Slog.w(TAG, "IOplusVibrator.playEffect failed, falling back to AOSP", t);
                }
            }
        }
        if (!shouldAllowStockHaptics()) {
            return;
        }
        if (mVibrator == null) {
            return;
        }
        // Plain Vibrator.vibrate() uses VibrationAttributes.USAGE_UNKNOWN, which the framework
        // resolves to the "media" vibration intensity. That is deliberate: like stock OPlus, the
        // camera shutter must still buzz even when the user has turned touch haptics off. Switch
        // to VibrationAttributes.createForUsage(USAGE_TOUCH) if it should follow the
        // touch-feedback slider instead.
        mVibrator.vibrate(toVibrationEffect(effect));
    }

    public void cancelVibrate(WaveformEffect effect) {
        IOplusVibrator svc = getOplusVibrator();
        if (svc != null) {
            try {
                svc.off();
            } catch (Throwable ignored) {
                synchronized (this) {
                    mOplusVibrator = null;
                }
            }
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    public int getVibratorStatus() {
        return 0; // always ready
    }

    public void setVibratorStrength(int strength) {
        // no-op
    }

    public int getSettingsTouchEffectStrength() {
        return 2400; // default max
    }

    public int getSettingsRingtoneEffectStrength() {
        return 2400;
    }

    public int getSettingsNotificationEffectStrength() {
        return 2400;
    }

    public int getVibratorTouchStyle() {
        return 0;
    }

    public void setVibratorTouchStyle(int style) {
        // no-op
    }

    public void updateVibrationAmplitude(float amplitudeRatio) {
        // no-op
    }

    public VibrationEffect createWaveformEffect(int effectId, int strength, int repeat) {
        return toVibrationEffect(null);
    }

    public VibrationEffect createWaveformEffect(String ringtonePath, int strength, int repeat) {
        return toVibrationEffect(null);
    }

    public void vibrate(VibrationEffect effect, android.os.VibrationAttributes attributes) {
        if (mVibrator == null) {
            return;
        }
        VibrationEffect eff = (effect != null) ? effect : toVibrationEffect(null);
        if (attributes != null) {
            mVibrator.vibrate(eff, attributes);
        } else {
            mVibrator.vibrate(eff);
        }
    }

    public void vibrate(VibrationEffect effect, String reason,
            android.os.VibrationAttributes attributes) {
        vibrate(effect, attributes);
    }

    public void vibrate(int uid, String opPkg, VibrationEffect effect, String reason,
            android.os.VibrationAttributes attributes) {
        vibrate(effect, attributes);
    }

    /*
     * The remaining OPlus service entry points, kept so this class matches
     * com.oplus.os.ILinearmotorVibratorService one for one. Nothing on the device calls them
     * today; they all resolve to the AOSP-backed implementations above.
     */

    public void vibrate(int uid, String opPkg, WaveformEffect effect, IBinder token) {
        vibrate(effect);
    }

    public void cancelVibrate(WaveformEffect effect, IBinder token) {
        cancelVibrate(effect);
    }

    public void updateVibrationAmplitude(int uid, String opPkg, float amplitudeRatio) {
        updateVibrationAmplitude(amplitudeRatio);
    }

    public boolean checkRichtapBlackList(String packageName) {
        // No richtap blacklist on AOSP: nothing is blocked.
        return false;
    }
}
