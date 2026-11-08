package com.fleet.ecocar.domain.navigation

import kotlin.math.abs
import kotlin.math.roundToInt

/** Driver distances: metres below 1000, kilometres with one decimal above. */
object NavFormat {
    fun formatDistance(meters: Double): String {
        val safe = meters.coerceAtLeast(0.0)
        if (safe < 1000.0) return "${safe.roundToInt()} m"
        return oneDecimal(safe / 1000.0) + " km"
    }

    fun formatDuration(seconds: Double): String {
        val minutes = (seconds.coerceAtLeast(0.0) / 60.0).roundToInt()
        val hours = minutes / 60
        val rest = minutes % 60
        return when {
            hours > 0 && rest > 0 -> "$hours h $rest min"
            hours > 0 -> "$hours h"
            else -> "$rest min"
        }
    }

    private fun oneDecimal(value: Double): String {
        val tenths = (value * 10.0).roundToInt().coerceAtLeast(0)
        val whole = tenths / 10
        val fraction = abs(tenths % 10)
        return "$whole,$fraction"
    }
}
