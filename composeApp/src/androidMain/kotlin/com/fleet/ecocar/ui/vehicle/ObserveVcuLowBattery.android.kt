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
import com.fleet.ecocar.domain.vehicle.LadestationSocPolicy
import com.fleet.ecocar.domain.vehicle.configuredLowBatteryPercent

@Composable
actual fun ObserveVcuLowBattery(onLowBattery: () -> Unit) {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val snapshot by app.batteryClient.batteryState.collectAsState()
    val policy = remember { LadestationSocPolicy(configuredLowBatteryPercent()) }
    var lowSocEpisodeShown by remember { mutableStateOf(false) }

    LaunchedEffect(snapshot?.timestamp, snapshot?.stateOfChargePercent) {
        val snap = snapshot ?: return@LaunchedEffect
        val hasLiveData = snap.timestamp > 0L
        val low = LadestationSocPolicy.isLowSoc(
            socPercent = snap.stateOfChargePercent,
            hasLiveData = hasLiveData,
            lowBatteryPercent = policy.lowBatteryPercent,
        )
        if (!hasLiveData) return@LaunchedEffect
        if (!low) {
            lowSocEpisodeShown = false
        } else if (!lowSocEpisodeShown) {
            lowSocEpisodeShown = true
            onLowBattery()
        }
    }
}
