package com.fleet.ecocar.domain.navigation

/**
 * Google encoded polyline with six decimal digits (1e-6 degrees).
 * Valhalla leg `shape` uses this precision.
 */
object Polyline6 {
    private const val PRECISION = 1_000_000.0

    fun decode(encoded: String): List<GeoPoint> {
        val points = ArrayList<GeoPoint>()
        var index = 0
        var latitude = 0
        var longitude = 0
        while (index < encoded.length) {
            val latitudeDelta = nextValue(encoded, index)
            index = latitudeDelta.nextIndex
            val longitudeDelta = nextValue(encoded, index)
            index = longitudeDelta.nextIndex
            latitude += latitudeDelta.value
            longitude += longitudeDelta.value
            points.add(GeoPoint(latitude / PRECISION, longitude / PRECISION))
        }
        return points
    }

    private fun nextValue(encoded: String, start: Int): Decoded {
        var result = 0
        var shift = 0
        var index = start
        while (index < encoded.length) {
            val chunk = encoded[index].code - 63
            index += 1
            result = result or ((chunk and 0x1f) shl shift)
            shift += 5
            if (chunk < 0x20) break
        }
        val signed = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        return Decoded(signed, index)
    }

    private class Decoded(val value: Int, val nextIndex: Int)
}
