package com.fleet.ecocar.navigation

import com.fleet.ecocar.domain.map.ChargingStation
import com.fleet.ecocar.domain.navigation.GeoPoint
import com.fleet.ecocar.domain.navigation.Maneuver
import com.fleet.ecocar.domain.navigation.Route
import com.fleet.ecocar.domain.navigation.RouteError
import com.fleet.ecocar.domain.navigation.RouteNavigation
import com.fleet.ecocar.domain.navigation.RoutePhase
import com.fleet.ecocar.domain.navigation.RoutePort
import com.fleet.ecocar.domain.navigation.RouteResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteNavigationTest {

    @Test
    fun noVehiclePositionDoesNotCallTheRouter() = runBlocking {
        val port = FakeRoutePort()
        val navigation = RouteNavigation(port, "https://router.example")
        navigation.select(ChargingStation("1", "Hof", 47.0, 8.0))
        navigation.start()
        assertEquals(0, port.calls)
        val phase = navigation.phase.value
        assertTrue(phase is RoutePhase.Failed)
        assertEquals(RouteError.NoVehiclePosition, (phase as RoutePhase.Failed).error)
    }

    @Test
    fun blankBaseUrlLeavesTheFeatureOff() = runBlocking {
        val port = FakeRoutePort()
        val navigation = RouteNavigation(port, "  ")
        assertTrue(!navigation.featureEnabled)
        navigation.updateVehicle(47.0, 8.0)
        navigation.select(ChargingStation("1", "Hof", 47.1, 8.1))
        navigation.start()
        assertEquals(0, port.calls)
        assertTrue(navigation.phase.value is RoutePhase.Idle)
    }

    @Test
    fun successBecomesActive() = runBlocking {
        val port = FakeRoutePort()
        port.result = RouteResult.Ok(sampleRoute())
        val navigation = RouteNavigation(port, "https://router.example")
        navigation.updateVehicle(0.0, 0.0)
        navigation.select(ChargingStation("1", "Hof", 0.0, 0.001))
        navigation.start()
        assertEquals(1, port.calls)
        assertTrue(navigation.phase.value is RoutePhase.Active)
    }

    @Test
    fun failureBecomesFailedAndVehicleUpdatesContinue() = runBlocking {
        val port = FakeRoutePort()
        port.result = RouteResult.Err(RouteError.Unreachable)
        val navigation = RouteNavigation(port, "https://router.example")
        navigation.updateVehicle(47.0, 8.0)
        navigation.select(ChargingStation("1", "Hof", 47.1, 8.1))
        navigation.start()
        val failed = navigation.phase.value
        assertTrue(failed is RoutePhase.Failed)
        assertEquals(RouteError.Unreachable, (failed as RoutePhase.Failed).error)
        navigation.updateVehicle(47.2, 8.2)
        assertTrue(navigation.phase.value is RoutePhase.Failed)
        assertEquals("Hof", navigation.selected.value?.name)
        assertEquals(1, port.calls)
    }

    private fun sampleRoute() = Route(
        shape = listOf(GeoPoint(0.0, 0.0), GeoPoint(0.0, 0.001)),
        maneuvers = listOf(Maneuver("Geradeaus", 0, 1, 100.0, 30.0)),
    )
}

private class FakeRoutePort : RoutePort {
    var calls = 0
    var result: RouteResult = RouteResult.Err(RouteError.NoRoute)
    override suspend fun route(
        origin: GeoPoint,
        destination: GeoPoint,
        headingDegrees: Double?,
    ): RouteResult {
        calls += 1
        return result
    }
}
