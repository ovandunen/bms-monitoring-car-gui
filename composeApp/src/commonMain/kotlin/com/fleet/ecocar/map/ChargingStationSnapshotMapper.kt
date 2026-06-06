package com.fleet.ecocar.map

/**
 * Maps BMS AIDL charging-station fields to EcoCar map model (testable without Android Parcelable).
 */
object ChargingStationSnapshotMapper {

    data class Fields(
        val stationId: String?,
        val displayName: String?,
        val streetAddress: String?,
        val city: String?,
        val latitude: Double,
        val longitude: Double,
        val solarCapacityKw: Double,
        val status: String?,
        val offlineCache: Boolean,
    )

    fun toEco(fields: Fields): EcoChargingStation =
        EcoChargingStation(
            stationId = fields.stationId.orEmpty(),
            displayName = fields.displayName?.takeIf { it.isNotBlank() } ?: fields.stationId.orEmpty(),
            streetAddress = fields.streetAddress,
            city = fields.city,
            latitude = fields.latitude,
            longitude = fields.longitude,
            solarCapacityKw = fields.solarCapacityKw,
            status = fields.status.orEmpty(),
            offlineCache = fields.offlineCache,
        )

    fun toEcoList(fields: List<Fields>): List<EcoChargingStation> = fields.map { toEco(it) }
}
