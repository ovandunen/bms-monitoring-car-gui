package com.fleet.ecocar.domain.navigation

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Sphere of radius [EARTH_RADIUS_METERS] (IUGG mean Earth radius, 6371 km).
 */
object GeoMath {
    const val EARTH_RADIUS_METERS = 6_371_000.0

    fun distanceMeters(from: GeoPoint, to: GeoPoint): Double {
        val lat1 = radians(from.latitude)
        val lat2 = radians(to.latitude)
        val dLat = lat2 - lat1
        val dLon = radians(to.longitude - from.longitude)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(h.coerceAtMost(1.0)))
    }

    /**
     * Closest point on the polyline. On a tie the later segment wins, so a
     * vehicle sitting on a vertex counts as having reached that vertex.
     */
    fun nearestOnRoute(shape: List<GeoPoint>, vehicle: GeoPoint): RouteSnap {
        if (shape.isEmpty()) {
            return RouteSnap(vehicle, 0.0, 0.0, 0, 0.0)
        }
        if (shape.size == 1) {
            return RouteSnap(shape[0], distanceMeters(shape[0], vehicle), 0.0, 0, 0.0)
        }
        var best: RouteSnap? = null
        var traveled = 0.0
        for (index in 0 until shape.lastIndex) {
            val start = shape[index]
            val end = shape[index + 1]
            val segment = distanceMeters(start, end)
            val fraction = if (segment == 0.0) 0.0 else project(start, end, vehicle).coerceIn(0.0, 1.0)
            val snapped = interpolate(start, end, fraction)
            val off = distanceMeters(snapped, vehicle)
            val candidate = RouteSnap(
                point = snapped,
                distanceOffMeters = off,
                alongMeters = traveled + segment * fraction,
                segmentIndex = index,
                fraction = fraction,
            )
            val current = best
            if (current == null ||
                off < current.distanceOffMeters - 1e-6 ||
                (kotlin.math.abs(off - current.distanceOffMeters) <= 1e-6 && index > current.segmentIndex)
            ) {
                best = candidate
            }
            traveled += segment
        }
        return best ?: RouteSnap(shape[0], distanceMeters(shape[0], vehicle), 0.0, 0, 0.0)
    }

    fun prefixMeters(shape: List<GeoPoint>): DoubleArray {
        val prefix = DoubleArray(shape.size)
        for (index in 1 until shape.size) {
            prefix[index] = prefix[index - 1] + distanceMeters(shape[index - 1], shape[index])
        }
        return prefix
    }

    private fun project(start: GeoPoint, end: GeoPoint, vehicle: GeoPoint): Double {
        val latScale = radians(1.0) * EARTH_RADIUS_METERS
        val lonScale = latScale * cos(radians(start.latitude))
        val abx = (end.longitude - start.longitude) * lonScale
        val aby = (end.latitude - start.latitude) * latScale
        val apx = (vehicle.longitude - start.longitude) * lonScale
        val apy = (vehicle.latitude - start.latitude) * latScale
        val lengthSquared = abx * abx + aby * aby
        if (lengthSquared == 0.0) return 0.0
        return (apx * abx + apy * aby) / lengthSquared
    }

    private fun interpolate(start: GeoPoint, end: GeoPoint, fraction: Double): GeoPoint =
        GeoPoint(
            start.latitude + (end.latitude - start.latitude) * fraction,
            start.longitude + (end.longitude - start.longitude) * fraction,
        )

    private fun radians(degrees: Double): Double = degrees * kotlin.math.PI / 180.0
}

class RouteSnap(
    val point: GeoPoint,
    val distanceOffMeters: Double,
    val alongMeters: Double,
    val segmentIndex: Int,
    val fraction: Double,
)
