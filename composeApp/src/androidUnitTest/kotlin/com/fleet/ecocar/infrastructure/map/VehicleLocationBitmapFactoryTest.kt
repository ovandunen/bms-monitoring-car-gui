package com.fleet.ecocar.infrastructure.map

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

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

    @Test
    fun nonSquareSource_longestSideIs96AndAspectWithinOnePixel() {
        val sourceWidth = 100
        val sourceHeight = 33
        val (width, height) = VehicleLocationBitmapFactory.bitmapSizePx(sourceWidth, sourceHeight)
        assertEquals(96, maxOf(width, height))
        val expectedHeight = VEHICLE_ICON_LONGEST_SIDE * sourceHeight.toDouble() / sourceWidth
        assertTrue(abs(height - expectedHeight) <= 1.0)

        val (portraitWidth, portraitHeight) = VehicleLocationBitmapFactory.bitmapSizePx(24, 80)
        assertEquals(96, maxOf(portraitWidth, portraitHeight))
        val expectedWidth = VEHICLE_ICON_LONGEST_SIDE * 24.0 / 80.0
        assertTrue(abs(portraitWidth - expectedWidth) <= 1.0)
    }
}

private const val VEHICLE_ICON_LONGEST_SIDE = 96.0
