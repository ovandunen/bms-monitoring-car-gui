package com.fleet.ecocar.domain.vehicle

/**
 * SOC threshold for Ladestation / low-battery UX.
 */
class LadestationSocPolicy(
    val lowBatteryPercent: Float = DEFAULT_LOW_BATTERY_PERCENT,
) {
    fun isLowBattery(socPercent: Float): Boolean = socPercent < lowBatteryPercent

    companion object {
        const val DEFAULT_LOW_BATTERY_PERCENT = 20f
        const val LOW_BATTERY_PERCENT = DEFAULT_LOW_BATTERY_PERCENT

        fun isLowSoc(socPercent: Float, hasLiveData: Boolean, lowBatteryPercent: Float): Boolean =
            hasLiveData && socPercent < lowBatteryPercent
    }
}
