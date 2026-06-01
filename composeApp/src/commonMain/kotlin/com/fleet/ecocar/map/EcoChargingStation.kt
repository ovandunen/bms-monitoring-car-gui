package com.fleet.ecocar.map

/**
 * Charging station for map/list in EcoCar GUI (from BMS AIDL / CSMS MQTT).
 *
 * Purple markers: [offlineCache] or status UNKNOWN (MQTT down, cached data).
 */
data class EcoChargingStation(
    val stationId: String,
    val displayName: String,
    val streetAddress: String?,
    val city: String?,
    val latitude: Double,
    val longitude: Double,
    val solarCapacityKw: Double,
    val status: String,
    val offlineCache: Boolean,
) {
    val isPurpleUnknown: Boolean =
        offlineCache || status.equals("UNKNOWN", ignoreCase = true)

    val addressLine: String? = listOfNotNull(streetAddress, city)
        .joinToString(", ")
        .ifBlank { null }
}
