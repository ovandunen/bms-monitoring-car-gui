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
) : Parcelable
