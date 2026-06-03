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
 * Genau sechs Nav-Bereiche laut Wireframe (Seite 3).
 */
enum class MainDestination(
    val icon: ImageVector,
) {
    Music(Icons.Filled.MusicNote),
    Map(Icons.Filled.Map),
    Battery(Icons.Filled.BatteryChargingFull),
    Charts(Icons.AutoMirrored.Filled.ShowChart),
    Browser(Icons.Filled.Language),
    Settings(Icons.Filled.Settings),
}
