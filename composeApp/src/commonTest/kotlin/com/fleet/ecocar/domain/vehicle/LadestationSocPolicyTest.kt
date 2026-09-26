package com.fleet.ecocar.domain.vehicle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LadestationSocPolicyTest {

    @Test
    fun defaultThresholdIsTwenty() {
        val policy = LadestationSocPolicy()
        assertEquals(20f, policy.lowBatteryPercent)
        assertFalse(policy.isLowBattery(20f))
        assertTrue(policy.isLowBattery(19.9f))
    }

    @Test
    fun injectedThresholdIsUsed() {
        val policy = LadestationSocPolicy(lowBatteryPercent = 15f)
        assertTrue(policy.isLowBattery(14.9f))
        assertFalse(policy.isLowBattery(15f))
    }
}
