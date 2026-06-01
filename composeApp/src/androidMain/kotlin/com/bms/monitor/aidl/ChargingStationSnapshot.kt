package com.bms.monitor.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** IPC payload for CSMS charging stations (see [ChargingStationSnapshot.aidl]). */
@Parcelize
data class ChargingStationSnapshot(
    val stationId: String? = null,
    val displayName: String? = null,
    val streetAddress: String? = null,
    val city: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val solarCapacityKw: Double = 0.0,
    val status: String? = null,
    val offlineCache: Boolean = false,
) : Parcelable
