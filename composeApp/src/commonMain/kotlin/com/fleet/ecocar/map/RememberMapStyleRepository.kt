package com.fleet.ecocar.map

import androidx.compose.runtime.Composable
import com.fleet.ecocar.domain.map.port.MapStyleRepository

@Composable
expect fun rememberMapStyleRepository(): MapStyleRepository
