package com.fleet.ecocar.infrastructure.navigation

import android.content.Context
import com.fleet.ecocar.domain.navigation.LatLon
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncher
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncherOptions
import org.maplibre.navigation.core.models.DirectionsResponse

object TurnByTurnNavigationLauncher {
    fun start(context: Context, directionsResponseJson: String) {
        val response = DirectionsResponse.fromJson(directionsResponseJson)
        val routes = response.routes
        require(routes.isNotEmpty()) { "Directions response has no routes" }
        val options = NavigationLauncherOptions.builder()
            .directionsRoute(routes.first())
            .shouldSimulateRoute(false)
            .build()
        NavigationLauncher.startNavigation(context, options)
    }

    fun start(context: Context, origin: LatLon, destination: LatLon, directionsResponseJson: String) {
        start(context, directionsResponseJson)
    }
}
