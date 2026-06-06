package com.fleet.ecocar.domain.map

import com.fleet.ecocar.map.EcoChargingStation

fun EcoChargingStation.toMapPin(): ChargingStation =
    ChargingStation(
        id = stationId,
        name = displayName,
        latitude = latitude,
        longitude = longitude,
    )
