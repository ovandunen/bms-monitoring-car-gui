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
        return result
    }
}
