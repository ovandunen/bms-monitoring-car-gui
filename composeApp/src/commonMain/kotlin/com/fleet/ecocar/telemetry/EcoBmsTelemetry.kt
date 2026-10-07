package com.fleet.ecocar.telemetry

/**
 * KMP-tauglicher Snapshot der vom BMS-Service per AIDL gelieferten Daten (ohne Android-Parcelable).
 */
data class EcoBmsTelemetry(
    val timestamp: Long,
    val cellVolts: List<Float>,
    val packTemperature: Float,
    val packHumidity: Float,
    val pm25: Int,
    val pm10: Int,
    val soc: Float,
    val currentA: Float,
    val ambientTemperatureC: Float = 0f,
    val humidity: Float = 0f,
    val sensorError: Boolean = false,
    val ds18b20Error: Boolean = false,
    val sht31Error: Boolean = false,
    val pms5003Error: Boolean = false,
)

data class EcoBmsAlert(
    val level: Int,
    val message: String,
)
