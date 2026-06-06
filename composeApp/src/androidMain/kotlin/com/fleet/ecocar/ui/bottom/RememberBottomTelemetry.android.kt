package com.fleet.ecocar.ui.bottom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication

@Composable
actual fun rememberBottomTelemetry(): BottomTelemetry {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val snapshot by app.batteryClient.batteryState.collectAsState()
        return remember(snapshot) {
            val live = snapshot?.takeIf { it.timestamp > 0L }
            BottomTelemetry(
                socPercent = live?.stateOfChargePercent?.toInt() ?: 0,
                tripDistanceKm = null,
                rangeKm = live?.estimatedRangeKm?.toDouble(),
            )
        }
}
