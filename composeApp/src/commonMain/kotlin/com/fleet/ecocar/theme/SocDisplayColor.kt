package com.fleet.ecocar.theme

import androidx.compose.ui.graphics.Color
import com.fleet.ecocar.domain.vehicle.LadestationSocPolicy

fun Float.socDisplayColor(
    lowBatteryPercent: Float,
    hasLiveData: Boolean,
): Color =
    if (LadestationSocPolicy.isLowSoc(this, hasLiveData, lowBatteryPercent)) {
        EcoCarColors.LowSocOrange
    } else {
        EcoCarColors.GoldenYellow
    }

fun Int.socDisplayColor(
    lowBatteryPercent: Float,
    hasLiveData: Boolean,
): Color = toFloat().socDisplayColor(lowBatteryPercent, hasLiveData)
