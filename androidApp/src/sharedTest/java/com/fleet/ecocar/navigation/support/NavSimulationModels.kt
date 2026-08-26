package com.fleet.ecocar.navigation.support

data class SimulatedPoint(
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    val timestampOffsetMs: Long = 0L,
)

data class StationTarget(
    val stationId: String,
    val latitude: Double,
    val longitude: Double,
    val arrivalRadiusMeters: Double,
)

data class StationReachedEvent(
    val stationId: String,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double,
    val timestampMs: Long,
)
