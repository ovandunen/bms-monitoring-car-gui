package com.fleet.ecocar.ui.bottom

import com.fleet.ecocar.theme.EcoCarColors
import kotlin.test.Test
import kotlin.test.assertEquals

class BottomStatusChipColorTest {

    @Test
    fun noBatteryDataAndOfflineChips_useLowSocOrange() {
        assertEquals(EcoCarColors.LowSocOrange, BottomStatusChipColor)
    }
}
