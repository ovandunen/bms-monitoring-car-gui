package com.fleet.ecocar.telemetry

/**
 * KMP-tauglicher Snapshot der vom BMS-Service per AIDL gelieferten Daten (ohne Android-Parcelable).
 */
data class EcoBmsTelemetry(
    val timestamp: Long,
    val cellVolts: List<Float>,
    /** Not used for Charts (external temp is DS18B20 via USB). Kept for API compatibility. */
    val packTemperature: Float,
    /** DS18B20 external (outside) temperature from ESP32 USB — Charts → Temperatur tab. */
    val ambientTemperatureC: Float = 0f,
    val packHumidity: Float,
    /** SHT31-D relative humidity from ESP32 USB sensor hub. */
    val humidity: Float = 0f,
    val pm25: Int,
    val pm10: Int,
    val soc: Float,
    val currentA: Float,
)
