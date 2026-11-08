package com.fleet.ecocar.infrastructure.navigation

import com.fleet.ecocar.domain.navigation.GeoPoint
import com.fleet.ecocar.domain.navigation.RouteError
import com.fleet.ecocar.domain.navigation.RoutePort
import com.fleet.ecocar.domain.navigation.RouteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * One origin, one destination, costing `auto`, language `de-DE`, units kilometres.
 * The published Valhalla example is `GET /route?json={...}` (turn-by-turn API reference).
 * [baseUrl] is the server root. This class appends `/route`.
 */
class ValhallaRoutePort(
    private val http: ValhallaHttp,
    private val baseUrl: String,
) : RoutePort {
    override suspend fun route(
        origin: GeoPoint,
        destination: GeoPoint,
        headingDegrees: Double?,
    ): RouteResult =
        withContext(Dispatchers.IO) {
            try {
                val json = URLEncoder.encode(
                    requestJson(origin, destination, headingDegrees),
                    Charsets.UTF_8.name(),
                )
                val url = baseUrl.trim().trimEnd('/') + "/route?json=" + json
                when (val outcome = http.get(url)) {
                    is ValhallaHttpResult.Timeout -> RouteResult.Err(RouteError.Timeout)
                    is ValhallaHttpResult.Unreachable -> RouteResult.Err(RouteError.Unreachable)
                    is ValhallaHttpResult.Response -> ValhallaResponse.parse(outcome.code, outcome.body)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                RouteResult.Err(RouteError.BadAnswer)
            }
        }

    internal fun requestJson(
        origin: GeoPoint,
        destination: GeoPoint,
        headingDegrees: Double?,
    ): String {
        val start = JSONObject().put("lat", origin.latitude).put("lon", origin.longitude)
        if (headingDegrees != null) start.put("heading", headingDegrees)
        return JSONObject()
            .put(
                "locations",
                JSONArray()
                    .put(start)
                    .put(JSONObject().put("lat", destination.latitude).put("lon", destination.longitude)),
            )
            .put("costing", "auto")
            .put(
                "directions_options",
                JSONObject().put("units", "kilometers").put("language", "de-DE"),
            )
            .toString()
    }
}
