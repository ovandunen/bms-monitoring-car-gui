package com.fleet.ecocar.domain.navigation

import com.fleet.ecocar.domain.map.ChargingStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class RoutePhase {
    data object Idle : RoutePhase()
    data object Loading : RoutePhase()
    class Active(val route: Route, val guidance: Guidance) : RoutePhase()
    class Failed(val error: RouteError) : RoutePhase()
}

/**
 * Selected station and one route. A blank [baseUrl] turns the feature off:
 * [start] does not call [port]. Network work stays inside [port].
 */
class RouteNavigation(
    private val port: RoutePort,
    baseUrl: String,
) {
    val featureEnabled: Boolean = baseUrl.trim().isNotEmpty()

    private val selectedState = MutableStateFlow<ChargingStation?>(null)
    val selected: StateFlow<ChargingStation?> = selectedState

    private val phaseState = MutableStateFlow<RoutePhase>(RoutePhase.Idle)
    val phase: StateFlow<RoutePhase> = phaseState

    private var vehicle: GeoPoint? = null
    private var heading: Double? = null
    private var route: Route? = null
    private var generation = 0

    fun updateVehicle(latitude: Double?, longitude: Double?, headingDegrees: Double? = null) {
        vehicle = if (latitude != null && longitude != null) GeoPoint(latitude, longitude) else null
        heading = if (vehicle == null) null else headingDegrees
        val current = route
        val here = vehicle
        if (current != null && here != null && phaseState.value is RoutePhase.Active) {
            phaseState.value = RoutePhase.Active(current, RouteGuidance.evaluate(current, here))
        }
    }

    /** Ignored while a route is loading or active, so the line is not swapped. */
    fun select(station: ChargingStation) {
        val phase = phaseState.value
        if (phase is RoutePhase.Loading || phase is RoutePhase.Active) return
        selectedState.value = station
        phaseState.value = RoutePhase.Idle
    }

    suspend fun start() {
        if (!featureEnabled) return
        val destination = selectedState.value ?: return
        val origin = vehicle
        val request = ++generation
        if (origin == null) {
            route = null
            phaseState.value = RoutePhase.Failed(RouteError.NoVehiclePosition)
            return
        }
        route = null
        phaseState.value = RoutePhase.Loading
        val result = try {
            port.route(
                origin,
                GeoPoint(destination.latitude, destination.longitude),
                heading,
            )
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            RouteResult.Err(RouteError.BadAnswer)
        }
        if (request != generation) return
        when (result) {
            is RouteResult.Ok -> {
                route = result.route
                val here = vehicle ?: origin
                phaseState.value = RoutePhase.Active(
                    result.route,
                    RouteGuidance.evaluate(result.route, here),
                )
            }
            is RouteResult.Err -> {
                route = null
                phaseState.value = RoutePhase.Failed(result.error)
            }
        }
    }

    fun end() {
        generation += 1
        route = null
        phaseState.value = RoutePhase.Idle
    }
}
