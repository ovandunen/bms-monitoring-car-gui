package com.bms.monitor.aidl

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SwapRecommendationSnapshot(
    val correlationId: String? = null,
    val stationId: String? = null,
    val validFrom: String? = null,
    val validUntil: String? = null,
    val reason: String? = null,
    val confidence: Double = 0.0,
    val routeEta: Int = 0,
) : Parcelable
