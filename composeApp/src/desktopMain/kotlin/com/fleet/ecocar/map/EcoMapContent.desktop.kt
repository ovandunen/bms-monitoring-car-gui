package com.fleet.ecocar.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.ui.main.PlaceholderScreen

/**
 * Desktop has no live map rendering yet (see TECHNICAL_DEBT.md) - this
 * stays a placeholder. vehicleLatitude/vehicleLongitude are accepted only
 * to satisfy the expect/actual contract; intentionally unused until
 * desktop gets its own MapLibre (or equivalent) wiring.
 */
@Composable
actual fun EcoMapContent(
    modifier: Modifier,
    stations: List<EcoChargingStation>,
    isRefreshing: Boolean,
    onRefreshStations: () -> Unit,
    vehicleLatitude: Double?,
    vehicleLongitude: Double?,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PlaceholderScreen(destination = MainDestination.Map)
    }
}
