package com.fleet.ecocar.theme

import androidx.compose.ui.graphics.Color
import com.fleet.ecocar.domain.vehicle.LadestationSocPolicy

fun Float.socDisplayColor(): Color =
    if (this in 0.01f..<LadestationSocPolicy.LOW_BATTERY_PERCENT) EcoCarColors.LowSocOrange
    else EcoCarColors.GoldenYellow

fun Int.socDisplayColor(): Color = toFloat().socDisplayColor()
