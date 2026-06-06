package com.fleet.ecocar.map

/**
 * Pure rules for when/how EcoCar requests Ladestationen from BMS over AIDL.
 *
 * Must stay aligned with BMS [com.ecocar.bms.domain.CsmsQueryDefaults] (CP-DEMO-001 seed).
 */
object ChargingStationMapRequestPolicy {

    /** CSMS demo station CP-DEMO-001 — same fallback BMS uses for low-SOC MQTT queries. */
    const val CSMS_DEMO_LATITUDE = 52.52
    const val CSMS_DEMO_LONGITUDE = 13.405

    data class Coordinates(val latitude: Double, val longitude: Double)

    /**
     * @param gpsFix null when fused location is unavailable on emulator/device.
     * Uses CSMS demo coordinates so [IBmsService.refreshChargingStations] can reach CP-DEMO-001.
     */
    fun coordinatesForBmsRefresh(gpsFix: Coordinates?): Coordinates =
        gpsFix ?: Coordinates(
            latitude = CSMS_DEMO_LATITUDE,
            longitude = CSMS_DEMO_LONGITUDE,
        )

    /** IPC/AIDL snapshot list replaces application state (BMS is source of truth). */
    fun applyIpcUpdate(incoming: List<EcoChargingStation>): List<EcoChargingStation> = incoming
}
