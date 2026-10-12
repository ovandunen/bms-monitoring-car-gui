package com.bms.monitor.aidl

import android.os.Parcel
import android.os.Parcelable

/**
 * Family B location parcel.
 *
 * The first four fields match the previous @Parcelize layout so an older
 * car GUI still reads latitude, longitude, time, and accuracy. Heading is
 * appended: int 0 means absent, int 1 is followed by a double. A missing
 * tail (old writer) is read as no heading, never as 0 degrees.
 */
data class BmsVehicleLocation(
    val latitude: Double,
    val longitude: Double,
    val timestampMillis: Long,
    val accuracyMeters: Float?,
    val headingDegrees: Double? = null,
) : Parcelable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(latitude)
        dest.writeDouble(longitude)
        dest.writeLong(timestampMillis)
        val accuracy = accuracyMeters
        if (accuracy == null) {
            dest.writeInt(0)
        } else {
            dest.writeInt(1)
            dest.writeFloat(accuracy)
        }
        val heading = headingDegrees
        if (heading == null) {
            dest.writeInt(0)
        } else {
            dest.writeInt(1)
            dest.writeDouble(heading)
        }
    }

    companion object CREATOR : Parcelable.Creator<BmsVehicleLocation> {
        override fun createFromParcel(parcel: Parcel): BmsVehicleLocation {
            val latitude = parcel.readDouble()
            val longitude = parcel.readDouble()
            val timestampMillis = parcel.readLong()
            val accuracyMeters = if (parcel.readInt() != 0) parcel.readFloat() else null
            val headingDegrees = readHeading(parcel)
            return BmsVehicleLocation(
                latitude = latitude,
                longitude = longitude,
                timestampMillis = timestampMillis,
                accuracyMeters = accuracyMeters,
                headingDegrees = headingDegrees,
            )
        }

        override fun newArray(size: Int): Array<BmsVehicleLocation?> = arrayOfNulls(size)

        private fun readHeading(parcel: Parcel): Double? {
            if (parcel.dataAvail() <= 0) return null
            if (parcel.readInt() == 0) return null
            return parcel.readDouble()
        }
    }
}
