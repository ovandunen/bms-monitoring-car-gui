package com.fleet.ecocar.theme

import com.fleet.ecocar.domain.vehicle.LadestationSocPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SocThresholdColourAndDialogTest {

    @Test
    fun threshold15_soc17NotLow_soc14LowAndDialogWouldTrigger() {
        val threshold = 15f
        val policy = LadestationSocPolicy(threshold)
        assertEquals(EcoCarColors.GoldenYellow, 17f.socDisplayColor(threshold))
        assertEquals(EcoCarColors.LowSocOrange, 14f.socDisplayColor(threshold))
        assertFalse(policy.isLowBattery(17f))
        assertTrue(policy.isLowBattery(14f))
    }
}
