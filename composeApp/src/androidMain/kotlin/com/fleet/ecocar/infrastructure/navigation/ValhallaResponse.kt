package com.fleet.ecocar.infrastructure.navigation

import com.fleet.ecocar.domain.navigation.GeoPoint
import com.fleet.ecocar.domain.navigation.Maneuver
import com.fleet.ecocar.domain.navigation.Polyline6
import com.fleet.ecocar.domain.navigation.Route
import com.fleet.ecocar.domain.navigation.RouteError
import com.fleet.ecocar.domain.navigation.RouteResult
import org.json.JSONObject

/**
 * Reads `trip.legs[].shape` (polyline precision 6) and each maneuver's
 * `instruction`, `length` (kilometres), `time` (seconds), and
 * `begin_shape_index` / `end_shape_index`.
 * Valhalla turn-by-turn reference, "Outputs of a route" and "Trip legs and maneuvers".
 */
object ValhallaResponse {
    fun parse(code: Int, body: String): RouteResult {
        if (code !in 200..299) return httpFailure(code, body)
        return try {
            val trip = JSONObject(body).optJSONObject("trip")
                ?: return RouteResult.Err(RouteError.NoRoute)
            val legs = trip.optJSONArray("legs") ?: return RouteResult.Err(RouteError.NoRoute)
            if (legs.length() == 0) return RouteResult.Err(RouteError.NoRoute)
            val shape = ArrayList<GeoPoint>()
            val maneuvers = ArrayList<Maneuver>()
            for (legIndex in 0 until legs.length()) {
                val leg = legs.optJSONObject(legIndex) ?: return RouteResult.Err(RouteError.BadAnswer)
                val offset = shape.size
                val encoded = leg.optString("shape", "")
                if (encoded.isNotEmpty()) shape.addAll(Polyline6.decode(encoded))
                val items = leg.optJSONArray("maneuvers") ?: return RouteResult.Err(RouteError.BadAnswer)
                if (items.length() == 0) return RouteResult.Err(RouteError.NoRoute)
                for (itemIndex in 0 until items.length()) {
                    val parsed = maneuver(items.optJSONObject(itemIndex), offset)
                        ?: return RouteResult.Err(RouteError.BadAnswer)
                    maneuvers.add(parsed)
                }
            }
            if (shape.size < 2 || maneuvers.isEmpty()) return RouteResult.Err(RouteError.NoRoute)
            RouteResult.Ok(Route(shape, maneuvers))
        } catch (_: Exception) {
            RouteResult.Err(RouteError.BadAnswer)
        }
    }

    private fun httpFailure(code: Int, body: String): RouteResult {
        if (code in 400..499 && hasErrorObject(body)) return RouteResult.Err(RouteError.NoRoute)
        if (code >= 500) return RouteResult.Err(RouteError.Unreachable)
        return RouteResult.Err(RouteError.BadAnswer)
    }

    private fun hasErrorObject(body: String): Boolean =
        try {
            val root = JSONObject(body)
            root.has("error") && !root.has("trip")
        } catch (_: Exception) {
            false
        }

    private fun maneuver(item: JSONObject?, offset: Int): Maneuver? {
        if (item == null) return null
        val instruction = item.optString("instruction", "")
        val lengthKm = item.optDouble("length", Double.NaN)
        val timeSeconds = item.optDouble("time", Double.NaN)
        val begin = item.optInt("begin_shape_index", -1)
        val end = item.optInt("end_shape_index", -1)
        if (instruction.isEmpty() || lengthKm.isNaN() || timeSeconds.isNaN() || begin < 0 || end < 0) {
            return null
        }
        return Maneuver(
            instruction = instruction,
            beginShapeIndex = begin + offset,
            endShapeIndex = end + offset,
            lengthMeters = lengthKm * 1000.0,
            timeSeconds = timeSeconds,
        )
    }
}
