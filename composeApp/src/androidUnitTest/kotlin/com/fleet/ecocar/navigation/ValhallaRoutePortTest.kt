package com.fleet.ecocar.navigation

import com.fleet.ecocar.domain.navigation.GeoPoint
import com.fleet.ecocar.domain.navigation.RouteError
import com.fleet.ecocar.domain.navigation.RouteResult
import com.fleet.ecocar.infrastructure.navigation.ValhallaHttp
import com.fleet.ecocar.infrastructure.navigation.ValhallaHttpResult
import com.fleet.ecocar.infrastructure.navigation.ValhallaResponse
import com.fleet.ecocar.infrastructure.navigation.ValhallaRoutePort
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

/**
 * Fixture follows the Valhalla turn-by-turn reference, "Trip legs and maneuvers":
 * leg `shape` is an encoded polyline with 6 decimal digits, and each maneuver has
 * `instruction`, `length` (kilometres when units are kilometers), `time` (seconds),
 * `begin_shape_index` and `end_shape_index`.
 * The shape "??_@?" is the hand fixture from [Polyline6Test].
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class ValhallaRoutePortTest {

    @Test
    fun parsesMinimalTrip() = runBlocking {
        var seen = ""
        val port = port(ValhallaHttp { url ->
            seen = url
            ValhallaHttpResult.Response(200, FIXTURE)
        })
        val result = port.route(GeoPoint(0.0, 0.0), GeoPoint(0.0, 0.001), null)
        assertTrue(result is RouteResult.Ok)
        val route = (result as RouteResult.Ok).route
        assertEquals(1, route.maneuvers.size)
        assertEquals("Rechts abbiegen", route.maneuvers[0].instruction)
        assertTrue(abs(route.maneuvers[0].lengthMeters - 111.0) < 0.001)
        assertTrue(abs(route.maneuvers[0].timeSeconds - 12.5) < 0.001)
        assertEquals(2, route.shape.size)
        assertTrue(abs(route.shape[1].latitude - 0.000016) < 1e-6)
        assertTrue(seen.startsWith("https://router.example/route?json="))
    }

    @Test
    fun requestUsesAutoCostingAndGerman() {
        val port = port(ValhallaHttp { ValhallaHttpResult.Timeout })
        val json = JSONObject(port.requestJson(GeoPoint(47.0, 8.0), GeoPoint(47.1, 8.1), null))
        assertEquals("auto", json.getString("costing"))
        assertEquals("de-DE", json.getJSONObject("directions_options").getString("language"))
        assertEquals("kilometers", json.getJSONObject("directions_options").getString("units"))
        assertEquals(2, json.getJSONArray("locations").length())
        assertTrue(!json.getJSONArray("locations").getJSONObject(0).has("heading"))
        val withHeading = JSONObject(port.requestJson(GeoPoint(47.0, 8.0), GeoPoint(47.1, 8.1), 90.0))
        assertTrue(abs(withHeading.getJSONArray("locations").getJSONObject(0).getDouble("heading") - 90.0) < 0.001)
    }

    @Test
    fun httpErrorIsUnreachable() = runBlocking {
        val result = port(ValhallaHttp {
            ValhallaHttpResult.Response(503, "down")
        }).route(GeoPoint(0.0, 0.0), GeoPoint(1.0, 1.0), null)
        assertEquals(RouteError.Unreachable, (result as RouteResult.Err).error)
    }

    @Test
    fun timeoutIsTimeout() = runBlocking {
        val result = port(ValhallaHttp { ValhallaHttpResult.Timeout })
            .route(GeoPoint(0.0, 0.0), GeoPoint(1.0, 1.0), null)
        assertEquals(RouteError.Timeout, (result as RouteResult.Err).error)
    }

    @Test
    fun emptyRouteIsNoRoute() {
        val parsed = ValhallaResponse.parse(200, """{"trip":{"legs":[]}}""")
        assertEquals(RouteError.NoRoute, (parsed as RouteResult.Err).error)
        val rejected = ValhallaResponse.parse(400, """{"error":"No path","error_code":442}""")
        assertEquals(RouteError.NoRoute, (rejected as RouteResult.Err).error)
    }

    @Test
    fun malformedJsonIsBadAnswerAndDoesNotThrow() = runBlocking {
        val parsed = ValhallaResponse.parse(200, "{")
        assertEquals(RouteError.BadAnswer, (parsed as RouteResult.Err).error)
        val thrown = port(ValhallaHttp { throw IllegalStateException("boom") })
            .route(GeoPoint(0.0, 0.0), GeoPoint(1.0, 1.0), null)
        assertEquals(RouteError.BadAnswer, (thrown as RouteResult.Err).error)
    }

    private fun port(http: ValhallaHttp) = ValhallaRoutePort(http, "https://router.example")

    private companion object {
        const val FIXTURE =
            """{"trip":{"legs":[{"shape":"??_@?","maneuvers":[{"instruction":"Rechts abbiegen","length":0.111,"time":12.5,"begin_shape_index":0,"end_shape_index":1}]}]}}"""
    }
}
