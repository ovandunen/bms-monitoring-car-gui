package com.fleet.ecocar.infrastructure.navigation

import com.graphhopper.util.PointList
import kotlin.math.pow
import kotlin.math.roundToLong

/** Encodes [PointList] to a Google-encoded polyline (precision 5 for Mapbox Directions). */
internal object PolylineEncoder {
    fun encode(points: PointList, precision: Int): String {
        val factor = 10.0.pow(precision)
        var lastLat = 0L
        var lastLon = 0L
        val result = StringBuilder()
        for (i in 0 until points.size()) {
            val lat = (points.getLat(i) * factor).roundToLong()
            val lon = (points.getLon(i) * factor).roundToLong()
            encodeValue(lat - lastLat, result)
            encodeValue(lon - lastLon, result)
            lastLat = lat
            lastLon = lon
        }
        return result.toString()
    }

    private fun encodeValue(value: Long, result: StringBuilder) {
        var v = if (value < 0) (value shl 1).inv() else value shl 1
        while (v >= 0x20) {
            result.append(((0x20 or (v and 0x1f).toInt()) + 63).toChar())
            v = v shr 5
        }
        result.append((v + 63).toChar())
    }
}
