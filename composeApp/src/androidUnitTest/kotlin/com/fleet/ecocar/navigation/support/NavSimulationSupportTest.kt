package com.fleet.ecocar.navigation.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** JVM unit tests for navigation simulation helpers (no device/emulator). */
class NavSimulationSupportTest {

    @Test
    fun haversineDistanceMeters_samePoint_isZero() {
        val distance = NavSimulationSupport.haversineDistanceMeters(
            lat1 = 14.7167,
            lon1 = -17.4677,
            lat2 = 14.7167,
            lon2 = -17.4677,
        )
        assertEquals(0.0, distance, 0.01)
    }

    @Test
    fun haversineDistanceMeters_knownSeparation_isWithinExpectedRange() {
        val distance = NavSimulationSupport.haversineDistanceMeters(
            lat1 = 14.7167,
            lon1 = -17.4677,
            lat2 = 14.7175,
            lon2 = -17.4660,
        )
        assertTrue(distance in 150.0..250.0)
    }

    @Test
    fun interpolateStraightLine_endpointsMatchFixture() {
        val points = NavSimulationSupport.interpolateStraightLine(
            startLat = 14.7167,
            startLng = -17.4677,
            endLat = 14.7175,
            endLng = -17.4660,
            stepCount = 5,
            intervalMsPerStep = 100L,
        )
        assertEquals(5, points.size)
        assertEquals(14.7167, points.first().latitude, 1e-6)
        assertEquals(-17.4677, points.first().longitude, 1e-6)
        assertEquals(14.7175, points.last().latitude, 1e-6)
        assertEquals(-17.4660, points.last().longitude, 1e-6)
        assertEquals(400L, points.last().timestampOffsetMs)
    }
}
