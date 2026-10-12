package com.fleet.ecocar.map

/**
 * GeoJSON for the MapLibre vehicle symbol. Bearing is degrees clockwise from
 * true north. 0 points the icon up on a north-up map. Null omits the property.
 */
internal object VehicleMarkerGeoJson {
    fun resolveBearing(incomingDegrees: Double?, retainedDegrees: Double?): Double? =
        if (incomingDegrees != null) normalize(incomingDegrees) else retainedDegrees

    fun toGeoJson(longitude: Double, latitude: Double, bearingDegrees: Double?): String {
        val properties = if (bearingDegrees == null) {
            "{}"
        } else {
            """{"bearing":${formatDegrees(normalize(bearingDegrees))}}"""
        }
        return """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[$longitude,$latitude]},"properties":$properties}]}"""
    }

    fun normalize(degrees: Double): Double {
        val wrapped = degrees % 360.0
        return if (wrapped < 0.0) wrapped + 360.0 else wrapped
    }

    private fun formatDegrees(degrees: Double): String {
        val whole = degrees.toLong()
        return if (degrees == whole.toDouble()) whole.toString() else degrees.toString()
    }
}
