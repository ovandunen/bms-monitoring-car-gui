package com.fleet.ecocar.map

import com.fleet.ecocar.domain.map.ChargingStation
import com.fleet.ecocar.domain.map.toMapPin

/**
 * Pure presentation rules for the map screen station list and pin layer.
 *
 * Both UI surfaces must consume the same [stations] list passed from
 * [rememberChargingStationMapState] (BMS AIDL → [com.fleet.ecocar.EcoCarApplication.chargingStations]).
 * A second ViewModel StateFlow caused an empty list while stations were available upstream.
 */
object EcoMapStationPresenter {

    fun mapPins(stations: List<EcoChargingStation>, highlightStationId: String? = null): List<ChargingStation> =
        stations.map { station ->
            station.toMapPin().copy(recommended = station.stationId == highlightStationId)
        }

    fun stationCount(stations: List<EcoChargingStation>): Int = stations.size

    fun shouldShowEmptyState(stations: List<EcoChargingStation>, isRefreshing: Boolean): Boolean =
        stations.isEmpty() && !isRefreshing

    fun shouldShowStationList(stations: List<EcoChargingStation>): Boolean =
        stations.isNotEmpty()
}
