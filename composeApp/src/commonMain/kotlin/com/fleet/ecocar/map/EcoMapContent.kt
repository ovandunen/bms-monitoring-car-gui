package com.fleet.ecocar.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * NOTE: default parameter values are only legal on the `expect` side in
 * Kotlin Multiplatform - each `actual` (Android, desktop) must declare the
 * same parameters WITHOUT repeating `= Modifier` / `= null`, or it's a
 * compile error ("actual function ... cannot have default values").
 */
@Composable
expect fun EcoMapContent(
    modifier: Modifier = Modifier,
    stations: List<EcoChargingStation>,
    isRefreshing: Boolean,
    onRefreshStations: () -> Unit,
    vehicleLatitude: Double? = null,
    vehicleLongitude: Double? = null,
)
