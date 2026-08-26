package com.fleet.ecocar

import com.fleet.ecocar.map.ChargingStationMapRequestPolicy
import kotlin.test.Test
import kotlin.test.assertEquals

class GpsFixForBmsRefreshTest {

    @Test
    fun mockLocation_releaseBuild_usesBerlinFallback() {
        val coords = ChargingStationMapRequestPolicy.coordinatesForBmsRefresh(
            resolveGpsFixForBmsRefresh(
                latitude = 48.0,
                longitude = 11.0,
                isFromMockProvider = true,
                acceptMockLocations = false,
            ),
        )

        assertEquals(ChargingStationMapRequestPolicy.CSMS_DEMO_LATITUDE, coords.latitude)
        assertEquals(ChargingStationMapRequestPolicy.CSMS_DEMO_LONGITUDE, coords.longitude)
    }

    @Test
    fun realLocation_releaseBuild_passesThroughUnchanged() {
        val coords = ChargingStationMapRequestPolicy.coordinatesForBmsRefresh(
            resolveGpsFixForBmsRefresh(
                latitude = 48.0,
                longitude = 11.0,
                isFromMockProvider = false,
                acceptMockLocations = false,
            ),
        )

        assertEquals(48.0, coords.latitude)
        assertEquals(11.0, coords.longitude)
    }

    @Test
    fun mockLocation_debugBuild_passesThroughUnchanged() {
        val coords = ChargingStationMapRequestPolicy.coordinatesForBmsRefresh(
            resolveGpsFixForBmsRefresh(
                latitude = 48.0,
                longitude = 11.0,
                isFromMockProvider = true,
                acceptMockLocations = true,
            ),
        )

        assertEquals(48.0, coords.latitude)
        assertEquals(11.0, coords.longitude)
    }
}
