package com.fleet.shared.battery.ui.application

import java.util.Locale

/**
 * Application DTO: uiautomator [contentDescription] values for battery overview integration tests.
 */
data class BatteryOverviewAutomationDescriptors(
    val soc: String,
    val voltage: String,
    val current: String,
    val power: String,
) {
    companion object {
        fun fromMetrics(
            socPercent: Float?,
            packVoltageV: Float?,
            packCurrentA: Float?,
            powerKw: Float?,
        ): BatteryOverviewAutomationDescriptors =
            BatteryOverviewAutomationDescriptors(
                soc = "battery-soc=${formatMetric(socPercent, 1)}",
                voltage = "battery-voltage=${formatMetric(packVoltageV, 1)}",
                current = "battery-current=${formatMetric(packCurrentA, 1)}",
                power = "battery-power=${formatMetric(powerKw, 2)}",
            )

        internal fun formatMetric(value: Float?, decimals: Int): String =
            value?.let { "%.${decimals}f".format(Locale.US, it) } ?: "–"
    }
}
