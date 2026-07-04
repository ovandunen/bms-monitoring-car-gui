package com.bms.monitor.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BmsData(
    val timestamp: Long,
    val cellVoltages: FloatArray,
    /** Pack temperature from CAN — not overwritten by USB ambient sensor. */
    val packTemperature: Float,
    val packHumidity: Float,
    val pm25: Int,
    val pm10: Int,
    val soc: Float,
    val current: Float,
    /** DS18B20 external (outside) temperature via ESP32 USB sensor hub. */
    val ambientTemperatureC: Float = 0f,
    /** SHT31-D relative humidity via ESP32 USB sensor hub. */
    val humidity: Float = 0f,
    val sensorNodeId: String = "",
    val sensorTsMs: Long = 0L,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BmsData
        if (timestamp != other.timestamp) return false
        if (!cellVoltages.contentEquals(other.cellVoltages)) return false
        if (packTemperature != other.packTemperature) return false
        if (packHumidity != other.packHumidity) return false
        if (pm25 != other.pm25) return false
        if (pm10 != other.pm10) return false
        if (soc != other.soc) return false
        if (current != other.current) return false
        if (ambientTemperatureC != other.ambientTemperatureC) return false
        if (humidity != other.humidity) return false
        if (sensorNodeId != other.sensorNodeId) return false
        if (sensorTsMs != other.sensorTsMs) return false
        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + cellVoltages.contentHashCode()
        result = 31 * result + packTemperature.hashCode()
        result = 31 * result + packHumidity.hashCode()
        result = 31 * result + pm25
        result = 31 * result + pm10
        result = 31 * result + soc.hashCode()
        result = 31 * result + current.hashCode()
        result = 31 * result + ambientTemperatureC.hashCode()
        result = 31 * result + humidity.hashCode()
        result = 31 * result + sensorNodeId.hashCode()
        result = 31 * result + sensorTsMs.hashCode()
        return result
    }
}
