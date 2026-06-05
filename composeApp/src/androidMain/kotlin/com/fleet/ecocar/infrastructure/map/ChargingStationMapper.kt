package com.fleet.ecocar.infrastructure.map

import com.fleet.ecocar.domain.map.ChargingStation
import com.fleet.ecocar.map.EcoChargingStation

internal object ChargingStationMapper {
    fun fromEco(station: EcoChargingStation): ChargingStation =
        ChargingStation(
            id = station.stationId,
            name = station.displayName,
            latitude = station.latitude,
            longitude = station.longitude,
        )
}
