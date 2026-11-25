package com.oplus.app;

import android.os.UserHandle;

import java.util.HashMap;
import java.util.Map;

public class OPlusAccessControlManager {
    private static volatile OPlusAccessControlManager sInstance = null;
    public static final int USER_CURRENT = UserHandle.myUserId();

    private OPlusAccessControlManager() {
    }

    public static OPlusAccessControlManager getInstance() {
        if (sInstance == null) {
            synchronized (OPlusAccessControlManager.class) {
                if (sInstance == null) {
                    sInstance = new OPlusAccessControlManager();
                }
            }
        }
        return sInstance;
    }

    public boolean isEncryptPass(String packageName, int userId) {
        return true;
    }

    public boolean isEncryptedPackage(String packageName, int userId) {
        return true;
    }

    public Map<String, Integer> getAccessControlAppsInfo(String type, int userId) {
        return new HashMap<>();
    }

    public boolean getAccessControlEnabled(String type, int userId) {
        return false;
    }

    public boolean getApplicationAccessControlEnabledAsUser(String packageName, int userId) {
        return false;
    }

    public Map<String, Integer> getPrivacyAppInfo(int userId) {
        return new HashMap<>();
    }

    public boolean registerAccessControlObserver(String type, IOplusAccessControlObserver observer) {
        return false;
    }

    public boolean unregisterAccessControlObserver(String type, IOplusAccessControlObserver observer) {
        return false;
    }
}
