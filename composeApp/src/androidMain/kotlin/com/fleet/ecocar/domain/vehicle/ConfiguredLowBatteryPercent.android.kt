package com.fleet.ecocar.domain.vehicle

import com.fleet.ecocar.composeapp.BuildConfig

actual fun configuredLowBatteryPercent(): Float = BuildConfig.LOW_BATTERY_PERCENT
