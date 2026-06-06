package com.fleet.ecocar.infrastructure.map

import com.fleet.ecocar.domain.map.ChargingStation

/**
 * Converts domain stations to a GeoJSON FeatureCollection string (no MapLibre / Android imports).
 */
class GeoJsonStationLayerAdapter {

    fun toGeoJson(stations: List<ChargingStation>): String =
        buildString {
            append("""{"type":"FeatureCollection","features":[""")
            append(
                stations.joinToString(",") { station ->
                    val id = escapeJson(station.id)
                    val name = escapeJson(station.name)
                    """{"type":"Feature","geometry":{"type":"Point","coordinates":[${station.longitude},${station.latitude}]},"properties":{"id":"$id","name":"$name"}}"""
                },
            )
            append("]}")
        }

    private fun escapeJson(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
}
