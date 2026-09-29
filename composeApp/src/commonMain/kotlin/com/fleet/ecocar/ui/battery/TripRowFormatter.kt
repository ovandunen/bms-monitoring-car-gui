package com.fleet.ecocar.ui.battery

import java.util.Locale

data class TripRowUi(
    val startEpochMillis: Long,
    val endText: String,
    val kmText: String,
    val kwhText: String,
    val consumptionText: String?,
)

object TripRowFormatter {
    const val OPEN_ENDED_AT = -1L

    fun format(
        startedAt: Long,
        endedAt: Long,
        distanceKm: Float,
        energyKwh: Float,
        runningLabel: String,
    ): TripRowUi {
        val consumption = if (distanceKm == 0f) null else energyKwh / distanceKm
        return TripRowUi(
            startEpochMillis = startedAt,
            endText = if (endedAt == OPEN_ENDED_AT) runningLabel else endedAt.toString(),
            kmText = "%.1f".format(Locale.US, distanceKm),
            kwhText = "%.2f".format(Locale.US, energyKwh),
            consumptionText = consumption?.let { "%.3f".format(Locale.US, it) },
        )
    }
}
