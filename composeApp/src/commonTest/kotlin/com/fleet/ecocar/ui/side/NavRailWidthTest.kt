package com.fleet.ecocar.ui.side

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class NavRailWidthTest {

    @Test
    fun navRailWidth_isExactly48Dp() {
        assertEquals(48.dp, NAV_RAIL_WIDTH)
    }
}
