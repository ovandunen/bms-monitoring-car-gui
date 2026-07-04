package com.fleet.ecocar.domain.navigation

/**
 * CSMS-advised swap station resolved to coordinates for on-device routing.
 */
data class NavigationDestination(
    val stationId: String,
    val label: String,
    val coordinates: LatLon,
)
