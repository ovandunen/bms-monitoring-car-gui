package com.fleet.ecocar.domain.navigation

/**
 * One driving route. [lengthMeters] on each maneuver is the router's length.
 * Guidance distances are computed from [shape], not from those lengths.
 */
class Route(
    val shape: List<GeoPoint>,
    val maneuvers: List<Maneuver>,
)

class Maneuver(
    val instruction: String,
    val beginShapeIndex: Int,
    val endShapeIndex: Int,
    val lengthMeters: Double,
    val timeSeconds: Double,
)

sealed class RouteResult {
    class Ok(val route: Route) : RouteResult()
    class Err(val error: RouteError) : RouteResult()
}

/** Failures the driver can be told about. No Valhalla types. */
sealed class RouteError {
    data object NoVehiclePosition : RouteError()
    data object NoRoute : RouteError()
    data object Unreachable : RouteError()
    data object Timeout : RouteError()
    data object BadAnswer : RouteError()
}

/** Origin and destination in, a [Route] or a [RouteError] out. */
interface RoutePort {
    suspend fun route(origin: GeoPoint, destination: GeoPoint, headingDegrees: Double?): RouteResult
}
