package com.fleet.ecocar.application.navigation

import com.fleet.ecocar.domain.navigation.LatLon
import com.fleet.ecocar.domain.navigation.NavigationDestination
import com.fleet.ecocar.map.EcoChargingStation

/**
 * Maps an accepted CSMS swap recommendation [stationId] to map coordinates
 * from the existing BMS IPC station list — no second MQTT subscription.
 */
class ResolveSwapDestinationUseCase {
    fun resolve(
        stationId: String,
        stations: List<EcoChargingStation>,
    ): NavigationDestination? {
        if (stationId.isBlank()) return null
        val station = stations.firstOrNull { it.stationId == stationId } ?: return null
        return NavigationDestination(
            stationId = station.stationId,
            label = station.displayName.ifBlank { station.stationId },
            coordinates = LatLon(station.latitude, station.longitude),
        )
    }
}
