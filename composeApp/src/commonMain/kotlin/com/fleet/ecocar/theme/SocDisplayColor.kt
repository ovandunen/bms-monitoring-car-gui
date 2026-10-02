package com.fleet.ecocar.theme

import androidx.compose.ui.graphics.Color
import com.fleet.ecocar.domain.vehicle.LadestationSocPolicy

fun Float.socDisplayColor(
    lowBatteryPercent: Float = LadestationSocPolicy.DEFAULT_LOW_BATTERY_PERCENT,
): Color =
    if (this in 0.01f..<lowBatteryPercent) EcoCarColors.LowSocOrange
    else EcoCarColors.GoldenYellow

fun Int.socDisplayColor(
    lowBatteryPercent: Float = LadestationSocPolicy.DEFAULT_LOW_BATTERY_PERCENT,
): Color = toFloat().socDisplayColor(lowBatteryPercent)
