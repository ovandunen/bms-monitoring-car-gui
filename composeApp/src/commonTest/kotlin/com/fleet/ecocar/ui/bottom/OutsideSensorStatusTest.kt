package com.fleet.ecocar.ui.bottom

import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OutsideSensorStatusTest {

    @Test
    fun ds18b20Error_namesOutsideTemperature() {
        assertEquals(
            "Sensor error: Outside temperature (DS18B20)",
            sensorErrorChipText(sensorError = true, ds18b20Error = true, sht31Error = false, pms5003Error = false),
        )
        assertEquals(
            listOf("Sensor error: Outside temperature (DS18B20)"),
            chips(ds18b20Error = true),
        )
    }

    @Test
    fun ds18b20AndPms5003_namesBoth() {
        assertEquals(
            "Sensor error: Outside temperature (DS18B20), PMS5003",
            sensorErrorChipText(sensorError = true, ds18b20Error = true, sht31Error = false, pms5003Error = true),
        )
    }

    @Test
    fun sensorErrorWithoutFlags_saysOutsideSensors() {
        assertEquals(
            "Sensor error: outside sensors",
            sensorErrorChipText(sensorError = true, ds18b20Error = false, sht31Error = false, pms5003Error = false),
        )
    }

    @Test
    fun clearedFlags_hideTheChip() {
        assertNull(sensorErrorChipText(false, false, false, false))
        assertTrue(chips().isEmpty())
    }

    @Test
    fun sensorErrorAndNoBatteryData_showBothChips() {
        assertEquals(
            listOf("No battery data", "Sensor error: outside sensors"),
            bottomStatusChipLabels(
                batteryDataStale = true,
                cloudConnected = true,
                sensorError = true,
                ds18b20Error = false,
                sht31Error = false,
                pms5003Error = false,
                noBatteryData = "No battery data",
                cloudOffline = "Offline",
            ),
        )
    }

    @Test
    fun telemetryFlags_followLatestBmsData() {
        val base = BottomTelemetry(batteryDataStale = true)
        val failed = EcoBmsTelemetry(
            timestamp = 1L,
            cellVolts = emptyList(),
            packTemperature = 0f,
            packHumidity = 0f,
            pm25 = 0,
            pm10 = 0,
            soc = 0f,
            currentA = 0f,
            sensorError = true,
            ds18b20Error = true,
        )
        val shown = base.withSensorErrors(failed)
        assertTrue(shown.batteryDataStale)
        assertTrue(shown.sensorError)
        assertTrue(shown.ds18b20Error)
        assertFalse(shown.pms5003Error)
        assertFalse(base.withSensorErrors(null).sensorError)
    }

    private fun chips(
        sensorError: Boolean = false,
        ds18b20Error: Boolean = false,
        sht31Error: Boolean = false,
        pms5003Error: Boolean = false,
    ) = bottomStatusChipLabels(
        batteryDataStale = false,
        cloudConnected = true,
        sensorError = sensorError,
        ds18b20Error = ds18b20Error,
        sht31Error = sht31Error,
        pms5003Error = pms5003Error,
        noBatteryData = "No battery data",
        cloudOffline = "Offline",
    )
}
