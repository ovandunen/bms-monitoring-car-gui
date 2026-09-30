package com.fleet.ecocar.infrastructure.map

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class VehicleLocationBitmapFactoryTest {

    @Test
    fun vehicleIconBitmap_usesDoubledPixelSize() {
        // Previous ICON_SIZE_PX was 48 (before doubling on 2026-09-30).
        assertEquals(2 * 48, VehicleLocationBitmapFactory.VEHICLE_ICON_SIZE_PX)

        val bitmap = VehicleLocationBitmapFactory.createBitmap(RuntimeEnvironment.getApplication())
        assertEquals(VehicleLocationBitmapFactory.VEHICLE_ICON_SIZE_PX, bitmap.width)
        assertEquals(VehicleLocationBitmapFactory.VEHICLE_ICON_SIZE_PX, bitmap.height)
    }
}
