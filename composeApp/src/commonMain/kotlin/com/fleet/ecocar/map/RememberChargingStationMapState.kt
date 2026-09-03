package com.fleet.ecocar.map

import androidx.compose.runtime.Composable

data class ChargingStationMapState(
    val stations: List<EcoChargingStation>,
    val isRefreshing: Boolean,
    val refresh: () -> Unit,
    // ADDED: vehicle position, following the exact same expect/actual
    // pattern already used for stations/isRefreshing/refresh above - each
    // platform's actual populates these from whatever Android/desktop
    // specific source it has, so commonMain stays free of any
    // EcoCarApplication/LocalContext reference.
    val vehicleLatitude: Double? = null,
    val vehicleLongitude: Double? = null,
)

@Composable
expect fun rememberChargingStationMapState(): ChargingStationMapState