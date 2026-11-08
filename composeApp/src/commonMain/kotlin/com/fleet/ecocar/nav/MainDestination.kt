package com.fleet.ecocar.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Side-rail destinations. The in-app browser stays out of the rail because
 * LineageOS already provides one.
 */
enum class MainDestination(
    val icon: ImageVector,
    val inRail: Boolean = true,
) {
    Music(Icons.Filled.MusicNote),
    Map(Icons.Filled.Map),
    Battery(Icons.Filled.BatteryChargingFull),
    Charts(Icons.AutoMirrored.Filled.ShowChart),
    Browser(Icons.Filled.Language, inRail = false),
    Settings(Icons.Filled.Settings),
    ;

    companion object {
        val railEntries: List<MainDestination> = entries.filter { it.inRail }
    }
}
