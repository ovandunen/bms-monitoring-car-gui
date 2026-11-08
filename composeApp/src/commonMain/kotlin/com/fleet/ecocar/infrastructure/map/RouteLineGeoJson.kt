package com.fleet.ecocar.infrastructure.map

import com.fleet.ecocar.domain.navigation.GeoPoint

/** One GeoJSON line for the active route. Null removes the line. */
object RouteLineGeoJson {
    fun toGeoJson(points: List<GeoPoint>): String? {
        if (points.size < 2) return null
        val coordinates = points.joinToString(",") { point ->
            "[${point.longitude},${point.latitude}]"
        }
        return buildString {
            append("""{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"LineString","coordinates":[""")
            append(coordinates)
            append("""]},"properties":{}}]}""")
        }
    }
}
