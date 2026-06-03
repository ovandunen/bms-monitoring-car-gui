package com.fleet.ecocar.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun EcoMapContent(
    modifier: Modifier,
    stations: List<EcoChargingStation>,
    onRefreshStations: () -> Unit,
)
