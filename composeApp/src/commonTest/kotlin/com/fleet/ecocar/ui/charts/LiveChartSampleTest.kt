package com.fleet.ecocar.ui.charts

import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
