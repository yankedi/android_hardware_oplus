/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "vendor.oplus.vibrator"

#include "include/OplusVibrator.h"

#include <log/log.h>

namespace aidl {
namespace vendor {
namespace oplus {
namespace hardware {
namespace vibrator {

using ::aidl::android::hardware::vibrator::EffectStrength;

OplusVibrator::OplusVibrator(
        std::shared_ptr<::aidl::android::hardware::vibrator::Vibrator> vib)
    : mVibrator(std::move(vib)) {}

ndk::ScopedAStatus OplusVibrator::playEffect(int32_t effectId, int32_t strength,
                                             int32_t* _aidl_return) {
    if (!mVibrator) {
        *_aidl_return = -1;
        return ndk::ScopedAStatus::ok();
    }

    EffectStrength es = EffectStrength::STRONG;
    if (strength == 0) {
        es = EffectStrength::LIGHT;
    } else if (strength == 1) {
        es = EffectStrength::MEDIUM;
    }

    long playLengthMs = 0;
    int ret = mVibrator->ff.playStockEffect(effectId, es, &playLengthMs);
    if (ret == 0) {
        *_aidl_return = static_cast<int32_t>(playLengthMs);
    } else {
        ALOGD("playStockEffect(%d, %d) returned %d", effectId, strength, ret);
        *_aidl_return = -1;
    }
    return ndk::ScopedAStatus::ok();
}

ndk::ScopedAStatus OplusVibrator::off() {
    if (mVibrator) {
        mVibrator->ff.offStockEffect();
    }
    return ndk::ScopedAStatus::ok();
}

}  // namespace vibrator
}  // namespace hardware
}  // namespace oplus
}  // namespace vendor
}  // namespace aidl
