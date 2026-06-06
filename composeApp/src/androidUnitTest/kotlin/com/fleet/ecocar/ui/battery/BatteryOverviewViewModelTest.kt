package com.fleet.ecocar.ui.battery

import com.fleet.shared.battery.ui.application.BatteryOverviewAutomationDescriptors
import com.fleet.shared.bms.ipc.domain.BatterySnapshot
import com.fleet.shared.bms.ipc.domain.ConnectionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * IPC snapshot → overview UI model → uiautomator descriptor strings for integration-test-ui.
 */
class BatteryOverviewViewModelTest {

    @Test
    fun connectedSnapshot_producesIntegrationTestDescriptors() {
        val snapshot = integrationTestSnapshot()
        val labels = BatteryOverviewLabels(
            screenTitle = "Battery",
            socLabel = "SOC",
            voltageLabel = "Voltage",
            currentLabel = "Current",
            powerLabel = "Power",
            liveHint = "Live",
            demoHint = "Demo",
            connectingHint = "Connecting",
            offlineHint = "Offline",
        )

        val model = snapshot.toOverviewUiModel(ConnectionStatus.Connected, labels)

        assertEquals(12f, model.socPercent)
        assertEquals(310f, model.packVoltageV)
        assertEquals(-9.8f, model.packCurrentA)

        val descriptors = BatteryOverviewAutomationDescriptors.fromMetrics(
            socPercent = model.socPercent,
            packVoltageV = model.packVoltageV,
            packCurrentA = model.packCurrentA,
            powerKw = model.powerKw,
        )

        assertEquals("battery-soc=12.0", descriptors.soc)
        assertEquals("battery-voltage=310.0", descriptors.voltage)
        assertEquals("battery-current=-9.8", descriptors.current)
        assertEquals("battery-power=-3.04", descriptors.power)
    }

    @Test
    fun snapshotWithoutTimestamp_yieldsPlaceholderDescriptors() {
        val snapshot = integrationTestSnapshot().copy(timestamp = 0L)
        val labels = BatteryOverviewLabels(
            screenTitle = "Battery",
            socLabel = "SOC",
            voltageLabel = "Voltage",
            currentLabel = "Current",
            powerLabel = "Power",
            liveHint = "Live",
            demoHint = "Demo",
            connectingHint = "Connecting",
            offlineHint = "Offline",
        )

        val model = snapshot.toOverviewUiModel(ConnectionStatus.Connected, labels)

        assertEquals(null, model.socPercent)

        val descriptors = BatteryOverviewAutomationDescriptors.fromMetrics(
            socPercent = model.socPercent,
            packVoltageV = model.packVoltageV,
            packCurrentA = model.packCurrentA,
            powerKw = model.powerKw,
        )

        assertEquals("battery-soc=–", descriptors.soc)
    }

    private fun integrationTestSnapshot(): BatterySnapshot =
        BatterySnapshot(
            timestamp = 1L,
            stateOfChargePercent = 12f,
            totalVoltage = 310f,
            current = -9.8f,
            cellVoltageMax = 0,
            cellVoltageMin = 0,
            batteryTempMax = 0,
            batteryTempMin = 0,
            controllerTemp = 0,
            motorTemp = 0,
            motorRpm = 0,
            vehicleSpeed = 0f,
        )
}
