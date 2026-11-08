package com.fleet.ecocar.ui.side

import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.ui.DriverTouch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SideNavWidthTest {

    @Test
    fun sideNavIsAtLeastOneDriverTouchTargetWide() {
        assertTrue(SideNavWidth >= DriverTouch.MinTarget)
    }

    @Test
    fun railLeavesTheBrowserOutBecauseLineageOsHasOne() {
        assertEquals(
            listOf(
                MainDestination.Music,
                MainDestination.Map,
                MainDestination.Battery,
                MainDestination.Charts,
                MainDestination.Settings,
            ),
            MainDestination.railEntries,
        )
    }
}
