package com.fleet.ecocar.navigation.support

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Pure helpers for navigation test route simulation (no Android / MapLibre deps). */
object NavSimulationSupport {

    private const val EARTH_RADIUS_M = 6_371_000.0

    fun haversineDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    /**
     * Linearly interpolates [stepCount] points from start to end (inclusive endpoints).
     * [intervalMsPerStep] is written into each point's [SimulatedPoint.timestampOffsetMs].
     */
    fun interpolateStraightLine(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        stepCount: Int,
        intervalMsPerStep: Long,
    ): List<SimulatedPoint> {
        require(stepCount >= 2) { "stepCount must be >= 2" }
        val bearing = initialBearingDegrees(startLat, startLng, endLat, endLng)
        return (0 until stepCount).map { index ->
            val fraction = index.toDouble() / (stepCount - 1)
            SimulatedPoint(
                latitude = startLat + (endLat - startLat) * fraction,
                longitude = startLng + (endLng - startLng) * fraction,
                bearing = bearing,
                timestampOffsetMs = index * intervalMsPerStep,
            )
        }
    }

    private fun initialBearingDegrees(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
    ): Float {
        val lat1 = Math.toRadians(startLat)
        val lat2 = Math.toRadians(endLat)
        val dLng = Math.toRadians(endLng - startLng)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        return ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
    }
}
