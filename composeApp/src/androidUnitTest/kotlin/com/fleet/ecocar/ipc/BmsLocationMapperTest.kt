package com.fleet.ecocar.ipc

import com.bms.monitor.aidl.BmsData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BmsLocationMapperTest {

    private fun reading(lat: Double?, lon: Double?) = BmsData(
        timestamp = 1_700_000_000_000L,
        cellVoltages = floatArrayOf(3.7f),
        packTemperature = 20f,
        packHumidity = 30f,
        pm25 = 1,
        pm10 = 2,
        soc = 80f,
        current = 5f,
        latitude = lat,
        longitude = lon,
    )

    @Test
    fun `returns null when latitude is missing`() {
        assertNull(BmsLocationMapper.toLocation(reading(lat = null, lon = 13.405)))
    }

    @Test
    fun `returns null when longitude is missing`() {
        assertNull(BmsLocationMapper.toLocation(reading(lat = 52.52, lon = null)))
    }

    @Test
    fun `returns null when both are missing`() {
        assertNull(BmsLocationMapper.toLocation(reading(lat = null, lon = null)))
    }

    @Test
    fun `maps latitude, longitude and timestamp when both are present`() {
        val location = BmsLocationMapper.toLocation(reading(lat = 52.52, lon = 13.405))

        assertEquals(52.52, location!!.latitude, 0.0001)
        assertEquals(13.405, location.longitude, 0.0001)
        assertEquals(1_700_000_000_000L, location.time)
    }
}