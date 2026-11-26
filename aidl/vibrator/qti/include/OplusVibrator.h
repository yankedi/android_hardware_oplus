/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <aidl/vendor/oplus/hardware/vibrator/BnOplusVibrator.h>
#include "Vibrator.h"

#include <memory>

namespace aidl {
namespace vendor {
namespace oplus {
namespace hardware {
namespace vibrator {

class OplusVibrator : public BnOplusVibrator {
  public:
    explicit OplusVibrator(std::shared_ptr<::aidl::android::hardware::vibrator::Vibrator> vib);

    ndk::ScopedAStatus playEffect(int32_t effectId, int32_t strength,
                                  int32_t* _aidl_return) override;
    ndk::ScopedAStatus off() override;

  private:
    std::shared_ptr<::aidl::android::hardware::vibrator::Vibrator> mVibrator;
};

}  // namespace vibrator
}  // namespace hardware
}  // namespace oplus
}  // namespace vendor
}  // namespace aidl
