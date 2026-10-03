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
        val labels = overviewLabels()

        val model = snapshot.toOverviewUiModel(ConnectionStatus.Connected, labels, 20f)

        assertEquals(12f, model.socPercent)
        assertEquals(310f, model.packVoltageV)
        assertEquals(-9.8f, model.packCurrentA)
        assertEquals(26.5f, model.batteryTempAvgC)
        assertEquals("Standby", model.vehicleStatusLabel)
        assertEquals("Offline", model.cloudStatusLabel)
        assertEquals(true, model.showOfflineChip)

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
        val labels = overviewLabels()

        val model = snapshot.toOverviewUiModel(ConnectionStatus.Connected, labels, 20f)

        assertEquals(null, model.socPercent)
        assertEquals(null, model.batteryTempAvgC)

        val descriptors = BatteryOverviewAutomationDescriptors.fromMetrics(
            socPercent = model.socPercent,
            packVoltageV = model.packVoltageV,
            packCurrentA = model.packCurrentA,
            powerKw = model.powerKw,
        )

        assertEquals("battery-soc=–", descriptors.soc        )
    }

    @Test
    fun staleSnapshot_showsNoBatteryDataChip() {
        val snapshot = integrationTestSnapshot().copy(batteryDataStale = true)
        val model = snapshot.toOverviewUiModel(ConnectionStatus.Connected, overviewLabels(), 20f)
        assertEquals(true, model.showNoBatteryDataChip)
        assertEquals("No battery data", model.noBatteryDataLabel)
        assertEquals("", model.statusHint)
        assertEquals(true, model.metricsStale)
        assertEquals("Driving", vehicleStatusText(1, "Driving", "Standby", "Charging"))
        assertEquals("Standby", vehicleStatusText(0, "Driving", "Standby", "Charging"))
        assertEquals("Charging", vehicleStatusText(2, "Driving", "Standby", "Charging"))
    }

    @Test
    fun offlineChip_hiddenWhenCloudConnectedAndFresh() {
        val snapshot = integrationTestSnapshot().copy(
            batteryDataStale = false,
            cloudConnected = true,
        )
        val model = snapshot.toOverviewUiModel(ConnectionStatus.Connected, overviewLabels(), 20f)
        assertEquals(false, model.showNoBatteryDataChip)
        assertEquals(false, model.showOfflineChip)
    }

    @Test
    fun versionMismatch_usesLocalizedErrorPanelMessage() {
        assertEquals(
            "BMS and EcoCar versions do not match — install both apps together",
            ipcErrorPanelMessage(
                com.fleet.shared.bms.ipc.IpcContract.VERSION_MISMATCH_REASON,
                "BMS and EcoCar versions do not match — install both apps together",
            ),
        )
    }

    @Test
    fun socIsLow_usesPassedThreshold() {
        val labels = overviewLabels()
        val at17 = integrationTestSnapshot().copy(
            stateOfChargePercent = 17f,
            cloudConnected = true,
        ).toOverviewUiModel(ConnectionStatus.Connected, labels, 15f)
        val at14 = integrationTestSnapshot().copy(
            stateOfChargePercent = 14f,
            cloudConnected = true,
        ).toOverviewUiModel(ConnectionStatus.Connected, labels, 15f)
        assertEquals(false, at17.socIsLow)
        assertEquals(true, at14.socIsLow)
    }

    private fun overviewLabels() = BatteryOverviewLabels(
        screenTitle = "Battery",
        socLabel = "SOC",
        voltageLabel = "Voltage",
        currentLabel = "Current",
        powerLabel = "Power",
        temperatureLabel = "Avg. temp",
        liveHint = "Live",
        demoHint = "Demo",
        connectingHint = "Connecting",
        offlineHint = "Offline",
        driving = "Driving",
        standby = "Standby",
        charging = "Charging",
        vehicleStatusTitle = "Status",
        cloudOnline = "Online",
        cloudOffline = "Offline",
        noBatteryData = "No battery data",
    )

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
            batteryTempAvg = 26.5f,
        )
}
