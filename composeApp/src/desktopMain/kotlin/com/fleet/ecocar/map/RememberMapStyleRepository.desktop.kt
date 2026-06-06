package com.fleet.ecocar.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.fleet.ecocar.domain.map.port.MapStyleRepository

@Composable
actual fun rememberMapStyleRepository(): MapStyleRepository =
    remember {
        object : MapStyleRepository {
            override fun getStyleUrl(): String = ""
        }
    }
