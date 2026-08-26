package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication
import com.fleet.ecocar.domain.vehicle.BatteryAlertEpisodePolicy

@Composable
actual fun ObserveVcuBatteryAlerts(
    onLowBattery: () -> Unit,
    onLastChance: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val snapshot by app.batteryClient.batteryState.collectAsState()
    var episodeState by remember { mutableStateOf(BatteryAlertEpisodePolicy.EpisodeState()) }

    LaunchedEffect(snapshot?.timestamp, snapshot?.stateOfChargePercent) {
        val snap = snapshot?.takeIf { it.timestamp > 0L } ?: return@LaunchedEffect
        val (nextState, event) = BatteryAlertEpisodePolicy.evaluate(
            socPercent = snap.stateOfChargePercent,
            state = episodeState,
        )
        episodeState = nextState
        when (event) {
            BatteryAlertEpisodePolicy.AlertEvent.ShowStufe2 -> onLowBattery()
            BatteryAlertEpisodePolicy.AlertEvent.ShowStufe3 -> onLastChance()
            null -> Unit
        }
    }
}
