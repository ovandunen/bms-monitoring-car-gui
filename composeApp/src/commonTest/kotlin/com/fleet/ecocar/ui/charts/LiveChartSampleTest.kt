package com.fleet.ecocar.ui.charts

import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiveChartSampleTest {

    @Test
    fun charts_useOutsideAirHumidityAndPm25() {
        val sample = liveChartSample(
            EcoBmsTelemetry(
                timestamp = 1L,
                cellVolts = emptyList(),
                packTemperature = 40f,
                packHumidity = 10f,
                pm25 = 87,
                pm10 = 142,
                soc = 50f,
                currentA = 1f,
                ambientTemperatureC = 34.2f,
                humidity = 38.1f,
                sensorError = false,
            ),
        )
        assertEquals(34.2f, sample.temperatureC)
        assertEquals(38.1f, sample.humidity)
        assertEquals(87f, sample.pm25)
    }

    @Test
    fun sensorChart_whenTimestampIsZero_showsNoSensorData() {
        val series = SensorChartSeries().withReading(reading(timestamp = 0L))
        assertFalse(series.showsLiveChart(nowMs = 31_000L))
        assertEquals(emptyList(), series.temperature)
        assertEquals(emptyList(), series.humidity)
        assertEquals(emptyList(), series.pm25)
    }

    @Test
    fun sensorChart_whenAgeIs29s_showsData() {
        val at = 1_700_000_000_000L
        val series = SensorChartSeries().withReading(
            reading(timestamp = at, ambient = 34.2f, humidity = 38.1f, pm25 = 87),
        )
        assertTrue(series.showsLiveChart(at + 29_000L))
        assertEquals(listOf(34.2f), series.temperature)
        assertEquals(listOf(38.1f), series.humidity)
        assertEquals(listOf(87f), series.pm25)
    }

    @Test
    fun sensorChart_whenAgeIs31s_showsNoSensorData() {
        val at = 1_700_000_000_000L
        val series = SensorChartSeries().withReading(reading(timestamp = at))
        assertFalse(series.showsLiveChart(at + 31_000L))
    }

    @Test
    fun sensorChart_whenNewerSampleArrivesAfterStale_showsDataAgain() {
        val at = 1_700_000_000_000L
        val stale = SensorChartSeries().withReading(
            reading(timestamp = at, ambient = 34.2f, humidity = 38.1f, pm25 = 87),
        )
        assertFalse(stale.showsLiveChart(at + 31_000L))
        val nextAt = at + 31_000L
        val next = stale.withReading(
            reading(timestamp = nextAt, ambient = 20f, humidity = 40f, pm25 = 12),
        )
        assertTrue(next.showsLiveChart(nextAt))
        assertEquals(listOf(34.2f, 20f), next.temperature)
        assertEquals(listOf(38.1f, 40f), next.humidity)
        assertEquals(listOf(87f, 12f), next.pm25)
    }

    private fun reading(
        timestamp: Long,
        ambient: Float = 34.2f,
        humidity: Float = 38.1f,
        pm25: Int = 87,
    ) = EcoBmsTelemetry(
        timestamp = timestamp,
        cellVolts = emptyList(),
        packTemperature = 40f,
        packHumidity = 10f,
        pm25 = pm25,
        pm10 = 142,
        soc = 50f,
        currentA = 1f,
        ambientTemperatureC = ambient,
        humidity = humidity,
    )
}
