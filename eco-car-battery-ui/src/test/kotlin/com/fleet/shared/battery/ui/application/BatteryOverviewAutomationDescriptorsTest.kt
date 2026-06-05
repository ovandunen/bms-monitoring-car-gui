package com.fleet.shared.battery.ui.application

import com.fleet.shared.bms.integration.IntegrationTestExpectations
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Use case: integration-test-ui uiautomator dump contains stable battery metric descriptors.
 * Expected values come from bms-monitoring-app integration-test.contract.properties.
 */
class BatteryOverviewAutomationDescriptorsTest {

    @Test
    fun descriptors_integrationTestTelemetry_matchMakefilePatterns() {
        val d = BatteryOverviewAutomationDescriptors.fromMetrics(
            socPercent = IntegrationTestExpectations.socPercent,
            packVoltageV = IntegrationTestExpectations.packVoltageV,
            packCurrentA = IntegrationTestExpectations.packCurrentA,
            powerKw = IntegrationTestExpectations.powerKw,
        )

        assertEquals(IntegrationTestExpectations.descriptorSoc, d.soc)
        assertEquals(IntegrationTestExpectations.descriptorVoltage, d.voltage)
        assertEquals(IntegrationTestExpectations.descriptorCurrent, d.current)
        assertEquals(IntegrationTestExpectations.descriptorPower, d.power)
    }

    @Test
    fun descriptors_whenMetricMissing_usePlaceholderToken() {
        val d = BatteryOverviewAutomationDescriptors.fromMetrics(
            socPercent = null,
            packVoltageV = IntegrationTestExpectations.packVoltageV,
            packCurrentA = null,
            powerKw = null,
        )

        assertEquals("battery-soc=–", d.soc)
        assertEquals(IntegrationTestExpectations.descriptorVoltage, d.voltage)
        assertEquals("battery-current=–", d.current)
        assertEquals("battery-power=–", d.power)
    }
}
