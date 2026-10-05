package com.bms.monitor.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BmsData(
    val timestamp: Long,
    val cellVoltages: FloatArray,
    val packTemperature: Float,
    val packHumidity: Float,
    val pm25: Int,
    val pm10: Int,
    val soc: Float,
    val current: Float,
    val ambientTemperatureC: Float = 0f,
    val humidity: Float = 0f,
    val sensorNodeId: String = "",
    val sensorTsMs: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val sensorError: Boolean = false,
    val ds18b20Error: Boolean = false,
    val sht31Error: Boolean = false,
    val pms5003Error: Boolean = false,
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
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (sensorError != other.sensorError) return false
        if (ds18b20Error != other.ds18b20Error) return false
        if (sht31Error != other.sht31Error) return false
        if (pms5003Error != other.pms5003Error) return false
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
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + sensorError.hashCode()
        result = 31 * result + ds18b20Error.hashCode()
        result = 31 * result + sht31Error.hashCode()
        result = 31 * result + pms5003Error.hashCode()
        return result
    }
}
