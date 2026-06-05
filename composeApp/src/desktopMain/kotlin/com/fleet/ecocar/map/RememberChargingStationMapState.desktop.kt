package com.fleet.ecocar.map

import androidx.compose.runtime.Composable

@Composable
actual fun rememberChargingStationMapState(): ChargingStationMapState =
    ChargingStationMapState(stations = emptyList(), isRefreshing = false, refresh = {})
