package com.fleet.ecocar.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fleet.ecocar.EcoCarApplication
import com.fleet.ecocar.domain.map.ChargingStation
import com.fleet.ecocar.domain.map.port.ChargingStationRepository
import com.fleet.ecocar.infrastructure.map.EcoCarChargingStationRepository
import kotlinx.coroutines.flow.StateFlow

class EcoMapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChargingStationRepository =
        EcoCarChargingStationRepository(application as EcoCarApplication, viewModelScope)

    val chargingStations: StateFlow<List<ChargingStation>> = repository.chargingStations
    val isRefreshing: StateFlow<Boolean> = repository.isRefreshing

    fun refreshStations() {
        repository.refreshStations()
    }
}
