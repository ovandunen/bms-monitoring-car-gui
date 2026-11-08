package com.fleet.ecocar.navigation

import com.fleet.ecocar.domain.navigation.GeoMath
import com.fleet.ecocar.domain.navigation.GeoPoint
import com.fleet.ecocar.domain.navigation.Polyline6
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Polyline6Test {

    @Test
    fun decodesHandBuiltDeltas() {
        // Precision is 1e-6 degrees. A coordinate integer is shifted left by 1.
        // Each 5-bit chunk is or-ed with 0x20 while more chunks follow, then +63.
        // 0 -> (0 << 1) = 0 -> char 63 '?'. (0, 0) is "??".
        // Delta latitude 16 (0.000016 deg): (16 << 1) = 32.
        //   First chunk (32 & 31) | 32 = 32 -> '_' (95). Remaining 1 -> '@' (64).
        //   Longitude 0 -> '?'. Second point is "_@?".
        // Delta latitude -1: (-1 << 1) = -2, bitwise not = 1 -> '@'. Then lon '?' .
        val twoPoints = Polyline6.decode("??_@?")
        assertEquals(2, twoPoints.size)
        close(0.0, twoPoints[0].latitude)
        close(0.0, twoPoints[0].longitude)
        close(0.000016, twoPoints[1].latitude)
        close(0.0, twoPoints[1].longitude)

        val negative = Polyline6.decode("??@?")
        assertEquals(2, negative.size)
        close(0.0, negative[0].latitude)
        close(-0.000001, negative[1].latitude)
        close(0.0, negative[1].longitude)
    }

    private fun close(expected: Double, actual: Double) {
        assertTrue(abs(expected - actual) < 1e-6, "expected $expected actual $actual")
    }
}

class GeoMathTest {

    @Test
    fun oneDegreeAlongTheEquator() {
        // IUGG mean Earth radius 6371 km. One degree of a great circle is
        // 2 * pi * R / 360. Source: IUGG mean radius, sphere, not a measured road.
        val expected = 2.0 * kotlin.math.PI * 6_371_000.0 / 360.0
        val actual = GeoMath.distanceMeters(GeoPoint(0.0, 0.0), GeoPoint(0.0, 1.0))
        assertTrue(abs(actual - expected) / expected < 0.005)
        assertEquals(6_371_000.0, GeoMath.EARTH_RADIUS_METERS)
    }
}
