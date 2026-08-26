package com.bms.monitor.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Vehicle location fix resolved per the source priority policy:
 * USB GPS ("USB_GPS") > Traccar ("TRACCAR") > Android system location ("ANDROID_SYSTEM").
 *
 * source is a plain String rather than an enum since AIDL/Parcelize round-trips it
 * as-is with no extra mapping step, consistent with the rest of this package.
 */
@Parcelize
data class VehicleLocationSnapshot(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMetersPerSecond: Float = 0f,
    val hasSpeed: Boolean = false,
    val bearingDegrees: Float = 0f,
    val hasBearing: Boolean = false,
    val source: String = "ANDROID_SYSTEM",
) : Parcelable
