package com.fleet.ecocar.map

import androidx.compose.runtime.Composable

data class ChargingStationMapState(
    val stations: List<EcoChargingStation>,
    val refresh: () -> Unit,
)

@Composable
expect fun rememberChargingStationMapState(): ChargingStationMapState
