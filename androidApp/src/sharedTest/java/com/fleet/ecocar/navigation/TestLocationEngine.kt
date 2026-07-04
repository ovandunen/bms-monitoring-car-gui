package com.fleet.ecocar.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.maplibre.navigation.core.location.Location
import org.maplibre.navigation.core.location.engine.LocationEngine

/**
 * Programmatic GPS for instrumented navigation tests.
 *
 * MapLibre Navigation 5.x exposes [LocationEngine.listenToLocation] (Kotlin Flow).
 * [simulateLocation] pushes fake fixes — no `adb shell geo fix`.
 */
class TestLocationEngine : LocationEngine {

    private val updates = MutableSharedFlow<Location>(extraBufferCapacity = 16, replay = 1)

    @Volatile
    private var lastLocation: Location? = null

    fun simulateLocation(lat: Double, lng: Double, bearing: Float) {
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

    override fun listenToLocation(request: LocationEngine.Request): Flow<Location> =
        updates.asSharedFlow()

    override suspend fun getLastLocation(): Location? = lastLocation

    private companion object {
        const val PROVIDER = "test-location-engine"
    }
}
