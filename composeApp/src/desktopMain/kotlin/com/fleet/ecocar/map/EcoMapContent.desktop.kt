package com.fleet.ecocar.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.ui.main.PlaceholderScreen

@Composable
actual fun EcoMapContent(
    modifier: Modifier,
    stations: List<EcoChargingStation>,
    onRefreshStations: () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PlaceholderScreen(destination = MainDestination.Map)
    }
}
