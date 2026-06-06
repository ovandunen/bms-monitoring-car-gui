package com.fleet.ecocar.domain.map

/**
 * Map pin value object (domain layer — no platform imports).
 */
data class ChargingStation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)
