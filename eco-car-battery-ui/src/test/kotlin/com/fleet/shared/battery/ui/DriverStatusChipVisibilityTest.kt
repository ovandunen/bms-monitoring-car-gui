package com.fleet.shared.battery.ui

import com.fleet.shared.battery.ui.internal.BatteryTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class DriverStatusChipVisibilityTest {

    @Test
    fun noBatteryDataAndOfflineChips_useLowSocOrange() {
        assertEquals(BatteryTheme.LowSocOrange, OverviewDriverStatusChipColor)
    }

    @Test
    fun stale_showsNoBatteryDataChip() {
        val model = sample(showNoBatteryDataChip = true, showOfflineChip = false)
        assertEquals(true, model.showNoBatteryDataChip)
        assertEquals(false, model.showOfflineChip)
    }

    @Test
    fun cloudDisconnected_showsOfflineChip() {
        val model = sample(showNoBatteryDataChip = false, showOfflineChip = true)
        assertEquals(false, model.showNoBatteryDataChip)
        assertEquals(true, model.showOfflineChip)
    }

    @Test
    fun freshAndOnline_hidesBothChips() {
        val model = sample(showNoBatteryDataChip = false, showOfflineChip = false)
        assertEquals(false, model.showNoBatteryDataChip)
        assertEquals(false, model.showOfflineChip)
    }

    private fun sample(
        showNoBatteryDataChip: Boolean,
        showOfflineChip: Boolean,
    ) = BatteryOverviewUiModel(
        socPercent = 50f,
        packVoltageV = 310f,
        packCurrentA = -1f,
        powerKw = -0.3f,
        batteryTempAvgC = 25f,
        screenTitle = "Battery",
        socLabel = "SOC",
        voltageLabel = "V",
        currentLabel = "A",
        powerLabel = "P",
        temperatureLabel = "T",
        statusHint = "",
        showNoBatteryDataChip = showNoBatteryDataChip,
        showOfflineChip = showOfflineChip,
        noBatteryDataLabel = "Keine Batteriedaten",
        offlineLabel = "Offline",
    )
}
