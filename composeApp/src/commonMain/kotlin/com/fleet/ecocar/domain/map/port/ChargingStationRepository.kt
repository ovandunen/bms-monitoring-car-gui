package com.fleet.ecocar.domain.map.port

import com.fleet.ecocar.domain.map.ChargingStation
import kotlinx.coroutines.flow.StateFlow

/**
 * Port: charging-station data for the map screen (sourced from BMS AIDL in infrastructure).
 */
interface ChargingStationRepository {
    val chargingStations: StateFlow<List<ChargingStation>>
    val isRefreshing: StateFlow<Boolean>
    fun refreshStations()
}
