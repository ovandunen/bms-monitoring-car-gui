package com.fleet.ecocar.domain.vehicle

actual fun configuredLowBatteryPercent(): Float =
    LadestationSocPolicy.DEFAULT_LOW_BATTERY_PERCENT
