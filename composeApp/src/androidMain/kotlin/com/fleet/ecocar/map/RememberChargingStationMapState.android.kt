package com.fleet.ecocar.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication

@Composable
actual fun rememberChargingStationMapState(): ChargingStationMapState {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val stations by app.chargingStations.collectAsState(initial = emptyList())
    val refresh = remember(app) { { app.refreshChargingStationsNearby() } }
    return ChargingStationMapState(stations = stations, refresh = refresh)
}
