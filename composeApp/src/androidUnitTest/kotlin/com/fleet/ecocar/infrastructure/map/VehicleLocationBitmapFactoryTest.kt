package com.fleet.ecocar.infrastructure.map

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VehicleLocationBitmapFactoryTest {

    @Test
    fun vehicleIconWidth_isTwicePreviousPixelSize() {
        // Previous ICON_SIZE_PX was 48 (before doubling on 2026-09-30).
        assertEquals(2 * 48, VehicleLocationBitmapFactory.VEHICLE_ICON_SIZE_PX)
    }

    @Test
    fun twoToOneDrawable_bitmapIs96By48() {
        assertEquals(96 to 48, VehicleLocationBitmapFactory.bitmapSizePx(48, 24))
    }

    @Test
    fun squareDrawable_bitmapIs96By96() {
        assertEquals(96 to 96, VehicleLocationBitmapFactory.bitmapSizePx(24, 24))
    }
}
