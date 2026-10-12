package com.fleet.ecocar.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VehicleMarkerBearingTest {

    @Test
    fun resolveBearing_whenNull_keepsLast() {
        assertNull(VehicleMarkerGeoJson.resolveBearing(null, null))
        assertEquals(84.0, VehicleMarkerGeoJson.resolveBearing(null, 84.0))
    }

    @Test
    fun resolveBearing_zero_isNorth() {
        assertEquals(0.0, VehicleMarkerGeoJson.resolveBearing(0.0, 84.0))
    }

    @Test
    fun resolveBearing_359_stays359() {
        assertEquals(359.0, VehicleMarkerGeoJson.resolveBearing(359.0, null))
    }

    @Test
    fun resolveBearing_wrapsAcross360() {
        assertEquals(1.0, VehicleMarkerGeoJson.resolveBearing(361.0, null))
        assertEquals(359.0, VehicleMarkerGeoJson.resolveBearing(-1.0, null))
    }

    @Test
    fun toGeoJson_writesBearingProperty() {
        val north = VehicleMarkerGeoJson.toGeoJson(-17.4677, 14.7167, 0.0)
        val almostNorth = VehicleMarkerGeoJson.toGeoJson(-17.4677, 14.7167, 359.0)
        val wrapped = VehicleMarkerGeoJson.toGeoJson(-17.4677, 14.7167, 361.0)
        val missing = VehicleMarkerGeoJson.toGeoJson(-17.4677, 14.7167, null)

        assertTrue(north.contains(""""bearing":0"""))
        assertTrue(almostNorth.contains(""""bearing":359"""))
        assertTrue(wrapped.contains(""""bearing":1"""))
        assertFalse(missing.contains("bearing"))
        assertTrue(missing.contains("-17.4677"))
        assertTrue(missing.contains("14.7167"))
    }
}
