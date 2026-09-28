package com.fleet.ecocar.domain.vehicle

import com.fleet.ecocar.composeapp.BuildConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LadestationSocPolicyBuildConfigTest {

    @Test
    fun policyReceivesBuildConfigLowBatteryPercent() {
        val policy = LadestationSocPolicy(BuildConfig.LOW_BATTERY_PERCENT)
        assertEquals(BuildConfig.LOW_BATTERY_PERCENT, policy.lowBatteryPercent)
        assertEquals(20f, policy.lowBatteryPercent)
    }
}
