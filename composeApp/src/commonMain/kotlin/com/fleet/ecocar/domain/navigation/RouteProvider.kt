package com.fleet.ecocar.domain.navigation

/**
 * Computes a turn-by-turn route between two points using the on-device routing graph.
 */
interface RouteProvider {
    /**
     * @return Mapbox-compatible [DirectionsResponse] JSON for MapLibre Navigation.
     */
    suspend fun route(origin: LatLon, destination: LatLon): Result<String>
}
