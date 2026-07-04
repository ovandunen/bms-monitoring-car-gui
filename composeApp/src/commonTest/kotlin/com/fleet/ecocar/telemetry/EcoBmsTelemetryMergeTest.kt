package com.fleet.ecocar.telemetry

import kotlin.test.Test
import kotlin.test.assertEquals

class EcoBmsTelemetryMergeTest {

    @Test
    fun mergeIpcUpdate_preservesDs18b20AmbientWhenIpcHasNoUsbSensors() {
        val usb = sampleUsb(ambientTemperatureC = 24.5f)
        val ipc = sampleIpc(soc = 50f)

        val merged = EcoBmsTelemetryMerge.mergeIpcUpdate(ipc, usb)

        assertEquals(50f, merged.soc)
        assertEquals(24.5f, merged.ambientTemperatureC)
        assertEquals(0f, merged.packTemperature)
    }

    @Test
    fun mergeUsbSensors_overlaysDs18b20OntoLiveIpcSnapshot() {
        val ipc = sampleIpc(soc = 42f)
        val usb = sampleUsb(ambientTemperatureC = 31.2f, humidity = 55f, pm25 = 12)

        val merged = EcoBmsTelemetryMerge.mergeUsbSensors(ipc, usb)

        assertEquals(42f, merged.soc)
        assertEquals(31.2f, merged.ambientTemperatureC)
        assertEquals(55f, merged.humidity)
        assertEquals(12, merged.pm25)
    }

    @Test
    fun mergeIpcUpdate_doesNotReplaceUsbAmbientWithZeroFromIpc() {
        val usb = sampleUsb(ambientTemperatureC = 18f)
        val ipc = sampleIpc(soc = 60f).copy(ambientTemperatureC = 0f)

        val merged = EcoBmsTelemetryMerge.mergeIpcUpdate(ipc, usb)

        assertEquals(18f, merged.ambientTemperatureC)
    }

    private fun sampleIpc(soc: Float) =
        EcoBmsTelemetry(
            timestamp = 1_000L,
            cellVolts = listOf(3.2f),
            packTemperature = 0f,
            ambientTemperatureC = 0f,
            packHumidity = 0f,
            humidity = 0f,
            pm25 = 0,
            pm10 = 0,
            soc = soc,
            currentA = -5f,
        )

    private fun sampleUsb(
        ambientTemperatureC: Float,
        humidity: Float = 40f,
        pm25: Int = 8,
    ) = EcoBmsTelemetry(
        timestamp = 2_000L,
        cellVolts = emptyList(),
        packTemperature = 0f,
        ambientTemperatureC = ambientTemperatureC,
        packHumidity = 0f,
        humidity = humidity,
        pm25 = pm25,
        pm10 = 0,
        soc = 0f,
        currentA = 0f,
    )
}
