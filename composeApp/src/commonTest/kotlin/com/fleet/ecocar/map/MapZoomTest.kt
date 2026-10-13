package com.fleet.ecocar.map

import kotlin.test.Test
import kotlin.test.assertEquals

class MapZoomTest {

    @Test
    fun stepUp_addsExactlyOne() {
        assertEquals(11.0, steppedMapZoom(10.0, MAP_ZOOM_STEP, MIN, MAX))
        assertEquals(15.25, steppedMapZoom(14.25, MAP_ZOOM_STEP, MIN, MAX))
    }

    @Test
    fun stepDown_subtractsExactlyOne() {
        assertEquals(9.0, steppedMapZoom(10.0, -MAP_ZOOM_STEP, MIN, MAX))
        assertEquals(13.25, steppedMapZoom(14.25, -MAP_ZOOM_STEP, MIN, MAX))
    }

    @Test
    fun clampsAtMinimum() {
        assertEquals(MIN, steppedMapZoom(MIN, -MAP_ZOOM_STEP, MIN, MAX))
        assertEquals(MIN, steppedMapZoom(0.4, -MAP_ZOOM_STEP, MIN, MAX))
    }

    @Test
    fun clampsAtMaximum() {
        assertEquals(MAX, steppedMapZoom(MAX, MAP_ZOOM_STEP, MIN, MAX))
        assertEquals(MAX, steppedMapZoom(25.0, MAP_ZOOM_STEP, MIN, MAX))
    }

    private companion object {
        const val MIN = 0.0
        const val MAX = 25.5
    }
}
