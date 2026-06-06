package com.fleet.ecocar.map

import com.fleet.ecocar.infrastructure.map.GeoJsonStationLayerAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression: map list and pins must stay in sync with the stations passed from BMS IPC.
 */
class EcoMapStationPresenterTest {

    @Test
    fun availableStations_mustNotShowEmptyState() {
        val stations = listOf(sampleStation())

        assertFalse(
            EcoMapStationPresenter.shouldShowEmptyState(stations, isRefreshing = false),
            "Non-empty station list must not show the empty-state copy",
        )
        assertTrue(EcoMapStationPresenter.shouldShowStationList(stations))
        assertEquals(1, EcoMapStationPresenter.stationCount(stations))
    }

    @Test
    fun mapPins_andList_mustShareSameStationCount() {
        val stations = listOf(
            sampleStation(stationId = "sn-1"),
            sampleStation(stationId = "sn-2", displayName = "Station B"),
        )

        assertEquals(stations.size, EcoMapStationPresenter.mapPins(stations).size)
    }

    @Test
    fun mapPins_feedGeoJsonWithOneFeaturePerStation() {
        val stations = listOf(
            sampleStation(stationId = "sn-1"),
            sampleStation(stationId = "sn-2", displayName = "Thiès Solar"),
        )
        val geoJson = GeoJsonStationLayerAdapter().toGeoJson(EcoMapStationPresenter.mapPins(stations))

        assertTrue(geoJson.contains("\"type\":\"FeatureCollection\""))
        assertTrue(geoJson.contains("sn-1"))
        assertTrue(geoJson.contains("sn-2"))
        assertTrue(geoJson.contains("Thiès Solar"))
        assertEquals(2, geoJson.split("\"type\":\"Feature\"").size - 1)
    }

    @Test
    fun emptyWhileRefreshing_mustNotShowEmptyState() {
        assertFalse(EcoMapStationPresenter.shouldShowEmptyState(emptyList(), isRefreshing = true))
    }

    @Test
    fun emptyWhenIdle_mustShowEmptyState() {
        assertTrue(EcoMapStationPresenter.shouldShowEmptyState(emptyList(), isRefreshing = false))
        assertFalse(EcoMapStationPresenter.shouldShowStationList(emptyList()))
    }

    private fun sampleStation(
        stationId: String = "sn-demo",
        displayName: String = "Dakar Solar Hub",
    ) = EcoChargingStation(
        stationId = stationId,
        displayName = displayName,
        streetAddress = "Route de Rufisque",
        city = "Dakar",
        latitude = 14.7167,
        longitude = -17.4677,
        solarCapacityKw = 120.0,
        status = "AVAILABLE",
        offlineCache = false,
    )
}
