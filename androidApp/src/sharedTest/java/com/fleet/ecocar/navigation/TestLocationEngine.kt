package com.fleet.ecocar.navigation

import com.fleet.ecocar.navigation.support.NavSimulationSupport
import com.fleet.ecocar.navigation.support.NavTestLogger
import com.fleet.ecocar.navigation.support.SimulatedPoint
import com.fleet.ecocar.navigation.support.StationReachedEvent
import com.fleet.ecocar.navigation.support.StationTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.engine.LocationEngine

/**
 * Programmatic GPS for instrumented navigation tests.
 *
 * MapLibre Navigation 5.x exposes [LocationEngine.listenToLocation] (Kotlin Flow).
 * [simulateLocation] pushes a single fix; [simulateRoute] plays a timed sequence.
 */
class TestLocationEngine : LocationEngine {

    private val updates = MutableSharedFlow<Location>(extraBufferCapacity = 16, replay = 1)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var routeJob: Job? = null

    private val _stationReached = MutableStateFlow<StationReachedEvent?>(null)
    val stationReached: StateFlow<StationReachedEvent?> = _stationReached.asStateFlow()

    @Volatile
    private var lastLocation: Location? = null

    @Volatile
    private var acceptingUpdates = true

    fun simulateLocation(lat: Double, lng: Double, bearing: Float) {
        if (!acceptingUpdates) return
        emitLocation(lat, lng, bearing, stationTarget = null)
    }

    /**
     * Plays [points] in order, waiting until each point's [SimulatedPoint.timestampOffsetMs]
     * elapses from sequence start. When [intervalMs] is used as fallback spacing for points
     * without explicit offsets (all zero), consecutive points are spaced by [intervalMs].
     */
    fun simulateRoute(
        points: List<SimulatedPoint>,
        intervalMs: Long = 500L,
        stationTarget: StationTarget? = null,
    ): Job {
        require(points.isNotEmpty()) { "points must not be empty" }
        stopRouteSimulation()
        acceptingUpdates = true
        _stationReached.value = null
        return scope.launch {
            val sequenceStartMs = System.currentTimeMillis()
            val useExplicitOffsets = points.any { it.timestampOffsetMs > 0L }
            for ((index, point) in points.withIndex()) {
                val waitMs = when {
                    useExplicitOffsets -> {
                        val targetOffset = point.timestampOffsetMs
                        val elapsed = System.currentTimeMillis() - sequenceStartMs
                        (targetOffset - elapsed).coerceAtLeast(0L)
                    }
                    index == 0 -> 0L
                    else -> intervalMs
                }
                if (waitMs > 0L) {
                    delay(waitMs)
                }
                emitLocation(
                    lat = point.latitude,
                    lng = point.longitude,
                    bearing = point.bearing,
                    stationTarget = stationTarget,
                )
            }
        }.also { routeJob = it }
    }

    /** Stops route playback and rejects further location emits until the next [simulateRoute]. */
    fun stop() {
        acceptingUpdates = false
        stopRouteSimulation()
    }

    /** True while the harness may emit locations; false after [stop]. */
    fun isAcceptingUpdates(): Boolean = acceptingUpdates

    fun stopRouteSimulation() {
        routeJob?.cancel()
        routeJob = null
    }

    fun shutdown() {
        stop()
        scope.cancel()
    }

    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> =
        updates.asSharedFlow()

    override suspend fun getLastLocation(): Location? = lastLocation

    private fun emitLocation(
        lat: Double,
        lng: Double,
        bearing: Float,
        stationTarget: StationTarget?,
    ) {
        if (!acceptingUpdates) return

        val distanceToStationM = stationTarget?.let { target ->
            NavSimulationSupport.haversineDistanceMeters(
                lat,
                lng,
                target.latitude,
                target.longitude,
            )
        }
        NavTestLogger.logLocationUpdate(lat, lng, distanceToStationM)

        stationTarget?.let { target ->
            val distanceM = distanceToStationM ?: return@let
            if (distanceM <= target.arrivalRadiusMeters && _stationReached.value == null) {
                val event = StationReachedEvent(
                    stationId = target.stationId,
                    latitude = lat,
                    longitude = lng,
                    distanceMeters = distanceM,
                    timestampMs = System.currentTimeMillis(),
                )
                _stationReached.value = event
                NavTestLogger.logStationReached(target.stationId)
            }
        }

        val location = Location(
            latitude = lat,
            longitude = lng,
            accuracyMeters = 3f,
            altitude = null,
            altitudeAccuracyMeters = null,
            mslAltitude = null,
            mslAltitudeAccuracyMeters = null,
            speedMetersPerSeconds = 12f,
            bearing = bearing,
            timeMilliseconds = System.currentTimeMillis(),
            provider = PROVIDER,
        )
        lastLocation = location
        updates.tryEmit(location)
    }

    private companion object {
        const val PROVIDER = "test-location-engine"
    }
}
