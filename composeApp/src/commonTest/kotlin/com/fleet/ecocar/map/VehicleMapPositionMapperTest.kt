package com.fleet.ecocar.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VehicleMapPositionMapperTest {

    /*

    private val nowMs = 1_000_000L

    @Test
    fun fromIpc_nullCoordinates_returnsNull() {
        assertNull(
            VehicleMapPositionMapper.fromIpc(
                latitude = null,
                longitude = 13.4,
                timestampMillis = nowMs,
                nowMillis = nowMs,
            ),
        )
        assertNull(
            VehicleMapPositionMapper.fromIpc(
                latitude = 52.5,
                longitude = null,
                timestampMillis = nowMs,
                nowMillis = nowMs,
            ),
        )
    }

    @Test
    fun fromIpc_staleTimestamp_returnsNull() {
        assertNull(
               VehicleMapPositionMapper.fromIpc(
                latitude = 52.52,
                longitude = 13.405,
                timestampMillis = nowMs - 31_000L,
                nowMillis = nowMs,
            ),
        )
    }

    @Test
    fun fromIpc_valid_mapsLatLon() {
        val position = VehicleMapPositionMapper.fromIpc(
            latitude = 14.7167,
            longitude = -17.4677,
            timestampMillis = nowMs - 1_000L,
            nowMillis = nowMs,
        )

        assertEquals(14.7167, position!!.latitude)
        assertEquals(-17.4677, position.longitude)
        assertEquals(nowMs - 1_000L, position.timestampMillis)
    }
    */
}
