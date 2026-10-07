package com.fleet.ecocar.ui.charts

import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

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
    fun sensorChart_whenNoBmsData_showsNoSensorDataAndNoPoints() {
        val series = SensorChartSeries()
        assertFalse(series.showsLiveChart())
        assertEquals(emptyList(), series.temperature)
        assertEquals(emptyList(), series.humidity)
        assertEquals(emptyList(), series.pm25)
    }

    @Test
    fun sensorChart_whenBmsDataArrives_usesOutsideAirHumidityAndPm25() {
        val clock = TestTimeSource()
        val series = SensorChartSeries().withReading(
            reading(ambient = 34.2f, humidity = 38.1f, pm25 = 87, packTemperature = 40f, packHumidity = 10f),
            clock,
        )
        assertTrue(series.showsLiveChart())
        assertEquals(listOf(34.2f), series.temperature)
        assertEquals(listOf(38.1f), series.humidity)
        assertEquals(listOf(87f), series.pm25)
    }

    @Test
    fun sensorChart_whenLastReadingOlderThan30s_showsNoSensorDataUntilNewReading() {
        val clock = TestTimeSource()
        val first = SensorChartSeries().withReading(
            reading(ambient = 34.2f, humidity = 38.1f, pm25 = 87),
            clock,
        )
        clock += 31.seconds
        assertFalse(first.showsLiveChart())
        val next = first.withReading(
            reading(ambient = 20f, humidity = 40f, pm25 = 12),
            clock,
        )
        assertTrue(next.showsLiveChart())
        assertEquals(listOf(34.2f, 20f), next.temperature)
        assertEquals(listOf(38.1f, 40f), next.humidity)
        assertEquals(listOf(87f, 12f), next.pm25)
    }

    private fun reading(
        ambient: Float,
        humidity: Float,
        pm25: Int,
        packTemperature: Float = 40f,
        packHumidity: Float = 10f,
    ) = EcoBmsTelemetry(
        timestamp = 1L,
        cellVolts = emptyList(),
        packTemperature = packTemperature,
        packHumidity = packHumidity,
        pm25 = pm25,
        pm10 = 142,
        soc = 50f,
        currentA = 1f,
        ambientTemperatureC = ambient,
        humidity = humidity,
    )
}
