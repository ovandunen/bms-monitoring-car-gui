package com.fleet.ecocar.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Ladestation list use case: stations from BMS IPC must reach the map presenter without being dropped.
 */
class ChargingStationMapLoadUseCaseTest {

    @Test
    fun gpsUnavailable_usesCsmsDemoCoordinatesNotNullIsland() {
        val coords = ChargingStationMapRequestPolicy.coordinatesForBmsRefresh(gpsFix = null)

        assertEquals(ChargingStationMapRequestPolicy.CSMS_DEMO_LATITUDE, coords.latitude)
        assertEquals(ChargingStationMapRequestPolicy.CSMS_DEMO_LONGITUDE, coords.longitude)
    }

    @Test
    fun ipcUpdate_withStations_mustPopulateApplicationState() {
        val incoming = listOf(dakarStation())

        val applied = ChargingStationMapRequestPolicy.applyIpcUpdate(incoming)

        assertEquals(1, applied.size)
        assertEquals("sn-dakar", applied.first().stationId)
    }

    @Test
    fun ipcUpdate_withStations_mustNotShowEmptyMapState() {
        val stations = ChargingStationMapRequestPolicy.applyIpcUpdate(
            ChargingStationSnapshotMapper.toEcoList(listOf(dakarFields())),
        )

        assertFalse(
            EcoMapStationPresenter.shouldShowEmptyState(stations, isRefreshing = false),
            "Ladestation list must not be empty after BMS IPC delivered stations",
        )
        assertTrue(EcoMapStationPresenter.shouldShowStationList(stations))
        assertEquals(stations.size, EcoMapStationPresenter.mapPins(stations).size)
    }

    @Test
    fun aidlSnapshotMapping_preservesStationIdAndCoordinates() {
        val eco = ChargingStationSnapshotMapper.toEco(dakarFields())

        assertEquals("sn-dakar", eco.stationId)
        assertEquals("Dakar Solar Hub", eco.displayName)
        assertEquals(14.7167, eco.latitude)
        assertEquals(-17.4677, eco.longitude)
        assertTrue(eco.status.equals("AVAILABLE", ignoreCase = true))
    }

    @Test
    fun emptyIpcUpdate_showsEmptyStateWhenNotRefreshing() {
        val stations = ChargingStationMapRequestPolicy.applyIpcUpdate(emptyList())

        assertTrue(EcoMapStationPresenter.shouldShowEmptyState(stations, isRefreshing = false))
        assertFalse(EcoMapStationPresenter.shouldShowStationList(stations))
    }

    private fun dakarFields() = ChargingStationSnapshotMapper.Fields(
        stationId = "sn-dakar",
        displayName = "Dakar Solar Hub",
        streetAddress = "Route de Rufisque",
        city = "Dakar",
        latitude = 14.7167,
        longitude = -17.4677,
        solarCapacityKw = 120.0,
        status = "AVAILABLE",
        offlineCache = false,
    )

    private fun dakarStation() = ChargingStationSnapshotMapper.toEco(dakarFields())
}
