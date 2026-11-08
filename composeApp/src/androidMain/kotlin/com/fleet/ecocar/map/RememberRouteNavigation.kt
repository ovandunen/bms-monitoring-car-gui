package com.fleet.ecocar.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.fleet.ecocar.composeapp.BuildConfig
import com.fleet.ecocar.domain.navigation.RouteNavigation
import com.fleet.ecocar.infrastructure.navigation.UrlConnectionValhallaHttp
import com.fleet.ecocar.infrastructure.navigation.ValhallaRoutePort

@Composable
fun rememberRouteNavigation(): RouteNavigation {
    val baseUrl = BuildConfig.VALHALLA_BASE_URL
    return remember(baseUrl) {
        RouteNavigation(
            port = ValhallaRoutePort(UrlConnectionValhallaHttp(), baseUrl),
            baseUrl = baseUrl,
        )
    }
}
