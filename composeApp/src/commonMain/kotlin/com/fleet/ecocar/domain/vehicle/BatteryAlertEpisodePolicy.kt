package com.fleet.ecocar.domain.vehicle

/**
 * When to show Stufe 2 (SOC ≤ 20 %) vs Stufe 3 (SOC ≤ 5 %) driver dialogs.
 * One dialog per episode; episodes reset when SOC recovers above the respective threshold.
 */
object BatteryAlertEpisodePolicy {

    data class EpisodeState(
        val stufe2Shown: Boolean = false,
        val stufe3Shown: Boolean = false,
    )

    sealed interface AlertEvent {
        data object ShowStufe2 : AlertEvent
        data object ShowStufe3 : AlertEvent
    }

    fun evaluate(socPercent: Float, state: EpisodeState): Pair<EpisodeState, AlertEvent?> {
        val soc = socPercent
        var next = state
        var event: AlertEvent? = null

        if (soc > LadestationSocPolicy.LOW_BATTERY_PERCENT) {
            next = next.copy(stufe2Shown = false)
        }
        if (soc > LadestationSocPolicy.LAST_CHANCE_BATTERY_PERCENT) {
            next = next.copy(stufe3Shown = false)
        }

        when {
            soc <= LadestationSocPolicy.LAST_CHANCE_BATTERY_PERCENT && !next.stufe3Shown -> {
                next = next.copy(stufe3Shown = true, stufe2Shown = true)
                event = AlertEvent.ShowStufe3
            }
            soc <= LadestationSocPolicy.LOW_BATTERY_PERCENT && !next.stufe2Shown -> {
                next = next.copy(stufe2Shown = true)
                event = AlertEvent.ShowStufe2
            }
        }

        return next to event
    }
}
