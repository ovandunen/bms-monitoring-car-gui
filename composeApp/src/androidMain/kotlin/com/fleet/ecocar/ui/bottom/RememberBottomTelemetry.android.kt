package com.fleet.ecocar.ui.bottom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication
import kotlin.math.roundToInt

@Composable
actual fun rememberBottomTelemetry(): BottomTelemetry {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val snapshot by app.batteryClient.batteryState.collectAsState()
        return remember(snapshot) {
            val live = snapshot?.takeIf { it.timestamp > 0L }
            BottomTelemetry(
                socPercent = live?.stateOfChargePercent?.toInt() ?: 0,
                tripDistanceKm = live?.tripDistanceKm?.takeIf { it >= 0.5f }?.roundToInt(),
                rangeKm = live?.estimatedRangeKm?.takeIf { it > 0f }?.toDouble(),
                co2SavingKg = live?.co2SavingKg?.takeIf { live.tripDistanceKm >= 0.5f }?.toDouble(),
            )
        }
}
