package com.fleet.ecocar.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.fleet.ecocar.domain.map.port.MapStyleRepository
import com.fleet.ecocar.infrastructure.map.MapTilerStyleAdapter

@Composable
actual fun rememberMapStyleRepository(): MapStyleRepository =
    remember { MapTilerStyleAdapter() }
