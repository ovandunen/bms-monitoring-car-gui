package com.fleet.ecocar.domain.vehicle

/**
 * SOC threshold for Ladestation / low-battery UX.
 * Must match BMS [LOW_SOC_THRESHOLD] (20) so GUI and station preload use the same VCU value.
 */
object LadestationSocPolicy {
    const val LOW_BATTERY_PERCENT = 20f

    /** Stufe 3 — letzte Tausch-Möglichkeit (Vertragsfolge). Must match product spec. */
    const val LAST_CHANCE_BATTERY_PERCENT = 5f
}
