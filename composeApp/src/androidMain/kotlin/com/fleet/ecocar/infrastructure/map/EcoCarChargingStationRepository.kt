package com.fleet.ecocar.infrastructure.map

import com.fleet.ecocar.EcoCarApplication
import com.fleet.ecocar.domain.map.ChargingStation
import com.fleet.ecocar.domain.map.port.ChargingStationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EcoCarChargingStationRepository(
    private val app: EcoCarApplication,
    scope: CoroutineScope,
) : ChargingStationRepository {

    override val chargingStations: StateFlow<List<ChargingStation>> =
        app.chargingStations
            .map { list -> list.map(ChargingStationMapper::fromEco) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    override val isRefreshing: StateFlow<Boolean> = app.chargingStationsRefreshing

    override fun refreshStations() {
        app.requestChargingStationsForMap()
    }
}
