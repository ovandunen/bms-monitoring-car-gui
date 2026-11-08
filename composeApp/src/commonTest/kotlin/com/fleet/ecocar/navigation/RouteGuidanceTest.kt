package com.fleet.ecocar.navigation

import com.fleet.ecocar.domain.navigation.GeoPoint
import com.fleet.ecocar.domain.navigation.Maneuver
import com.fleet.ecocar.domain.navigation.Route
import com.fleet.ecocar.domain.navigation.RouteGuidance
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Four shape points on the equator, 0.001 degree apart.
 * Maneuvers begin at indexes 0 ("Los"), 1 ("Geradeaus") and 2 ("Ankommen").
 * The active maneuver is the first begin index strictly ahead of the snapped segment.
 * One segment is R * 0.001 * pi / 180 metres on this sphere.
 */
class RouteGuidanceTest {

    @Test
    fun activeManeuverAtFivePositions() {
        val segment = 6_371_000.0 * 0.001 * PI / 180.0
        check(GeoPoint(0.0, 0.0), "Geradeaus", segment, 3 * segment, 60.0)
        check(GeoPoint(0.0, 0.0005), "Geradeaus", 0.5 * segment, 2.5 * segment, 50.0)
        check(GeoPoint(0.0, 0.001), "Ankommen", segment, 2 * segment, 40.0)
        check(GeoPoint(0.0, 0.0025), "Ankommen", 0.0, 0.5 * segment, 10.0)
        check(GeoPoint(0.0, 0.003), "Ankommen", 0.0, 0.0, 0.0)
    }

    @Test
    fun offRouteFlagFollowsThreshold() {
        assertEquals(50.0, RouteGuidance.OFF_ROUTE_METERS)
        val metresPerDegree = 6_371_000.0 * PI / 180.0
        val far = RouteGuidance.evaluate(
            route(),
            GeoPoint(RouteGuidance.OFF_ROUTE_METERS * 2 / metresPerDegree, 0.0005),
        )
        val near = RouteGuidance.evaluate(
            route(),
            GeoPoint(RouteGuidance.OFF_ROUTE_METERS * 0.2 / metresPerDegree, 0.0005),
        )
        assertTrue(far.offRoute)
        assertFalse(near.offRoute)
    }

    private fun check(
        vehicle: GeoPoint,
        text: String,
        distance: Double,
        remaining: Double,
        seconds: Double,
    ) {
        val guidance = RouteGuidance.evaluate(route(), vehicle)
        assertEquals(text, guidance.maneuverText)
        close(distance, guidance.distanceToManeuverMeters)
        close(remaining, guidance.remainingMeters)
        close(seconds, guidance.remainingSeconds)
        assertFalse(guidance.offRoute)
    }

    private fun close(expected: Double, actual: Double) {
        assertTrue(abs(expected - actual) < 0.5, "expected $expected actual $actual")
    }

    private fun route(): Route = Route(
        shape = listOf(
            GeoPoint(0.0, 0.0),
            GeoPoint(0.0, 0.001),
            GeoPoint(0.0, 0.002),
            GeoPoint(0.0, 0.003),
        ),
        maneuvers = listOf(
            Maneuver("Los", 0, 1, 1.0, 10.0),
            Maneuver("Geradeaus", 1, 2, 1.0, 20.0),
            Maneuver("Ankommen", 2, 3, 1.0, 30.0),
        ),
    )
}
