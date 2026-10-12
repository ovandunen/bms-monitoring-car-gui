package com.bms.monitor.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleLocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val timestamp: Long,  // Unix timestamp in milliseconds
    val accuracy: Float? = null,  // Optional: accuracy in meters
    /** Degrees clockwise from true north. Null when the fix has no heading. */
    val headingDegrees: Double? = null,
) : Parcelable
