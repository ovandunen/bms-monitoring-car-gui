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
        assertEquals(EcoCarColors.GoldenYellow, 17f.socDisplayColor(threshold, hasLiveData = true))
        assertEquals(EcoCarColors.LowSocOrange, 14f.socDisplayColor(threshold, hasLiveData = true))
        assertFalse(policy.isLowBattery(17f))
        assertTrue(policy.isLowBattery(14f))
    }

    @Test
    fun colourMatchesIsLowSocForTheSameInput() {
        val threshold = 20f
        val cases = listOf(
            Triple(0f, false, false),
            Triple(0f, true, true),
            Triple(19.9f, true, true),
            Triple(20f, true, false),
        )
        for ((soc, hasLiveData, expectedLow) in cases) {
            val low = LadestationSocPolicy.isLowSoc(soc, hasLiveData, threshold)
            assertEquals(expectedLow, low)
            val color = soc.socDisplayColor(threshold, hasLiveData)
            assertEquals(low, color == EcoCarColors.LowSocOrange)
        }
    }
}
