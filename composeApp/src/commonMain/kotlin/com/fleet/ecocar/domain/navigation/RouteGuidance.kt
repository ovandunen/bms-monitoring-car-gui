package com.fleet.ecocar.domain.navigation

/**
 * Distances along the route shape from the vehicle's nearest point.
 * The active maneuver is the first whose begin index is strictly ahead of
 * that segment. If none is ahead, the last maneuver is shown at distance 0.
 * Remaining time is the maneuver times scaled by the remaining shape fraction.
 * No new route is requested here.
 */
object RouteGuidance {
    /**
     * Proposed, not tuned. Tablet GPS is often 5–15 m; 50 m stays past that
     * noise and still flags a vehicle that has left the street.
     */
    const val OFF_ROUTE_METERS = 50.0

    fun evaluate(route: Route, vehicle: GeoPoint): Guidance {
        if (route.shape.isEmpty() || route.maneuvers.isEmpty()) {
            return Guidance("", 0.0, 0.0, 0.0, false)
        }
        val snap = GeoMath.nearestOnRoute(route.shape, vehicle)
        val prefix = GeoMath.prefixMeters(route.shape)
        val ahead = route.maneuvers.firstOrNull { it.beginShapeIndex > snap.segmentIndex }
        val maneuver = ahead ?: route.maneuvers.last()
        val total = prefix.last()
        val remaining = (total - snap.alongMeters).coerceAtLeast(0.0)
        val distanceToManeuver = if (ahead == null) {
            0.0
        } else {
            val beginIndex = maneuver.beginShapeIndex.coerceIn(0, prefix.lastIndex)
            (prefix[beginIndex] - snap.alongMeters).coerceAtLeast(0.0)
        }
        val remainingSeconds = if (total <= 0.0) {
            0.0
        } else {
            route.maneuvers.sumOf { it.timeSeconds } * remaining / total
        }
        return Guidance(
            maneuverText = maneuver.instruction,
            distanceToManeuverMeters = distanceToManeuver,
            remainingMeters = remaining,
            remainingSeconds = remainingSeconds,
            offRoute = snap.distanceOffMeters > OFF_ROUTE_METERS,
        )
    }
}

class Guidance(
    val maneuverText: String,
    val distanceToManeuverMeters: Double,
    val remainingMeters: Double,
    val remainingSeconds: Double,
    val offRoute: Boolean,
)
