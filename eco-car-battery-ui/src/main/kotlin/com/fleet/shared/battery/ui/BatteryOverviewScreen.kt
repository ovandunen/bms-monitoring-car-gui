package com.fleet.shared.battery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fleet.shared.battery.ui.application.BatteryOverviewAutomationDescriptors
import com.fleet.shared.battery.ui.internal.BatteryTheme
import com.fleet.shared.battery.ui.internal.MetricCard
import com.fleet.shared.battery.ui.internal.StatusHintRow
import com.fleet.shared.battery.ui.internal.formatMetric

/**
 * Shared driver-facing battery overview (port). Stateless — callers supply [BatteryOverviewUiModel].
 */
@Composable
fun BatteryOverviewScreen(
    model: BatteryOverviewUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = model.screenTitle,
            style = MaterialTheme.typography.titleLarge,
            color = BatteryTheme.OnDark,
        )
        StatusHintRow(
            statusHint = model.statusHint,
            showProgress = model.showProgress,
            progress = model.progress,
            progressColor = if (model.socIsLow) BatteryTheme.LowSocOrange else BatteryTheme.GoldenYellow,
        )
        val automation = BatteryOverviewAutomationDescriptors.fromMetrics(
            socPercent = model.socPercent,
            packVoltageV = model.packVoltageV,
            packCurrentA = model.packCurrentA,
            powerKw = model.powerKw,
        )
        val socColor = when {
            model.metricsStale -> BatteryTheme.OnDarkSecondary
            model.socIsLow -> BatteryTheme.LowSocOrange
            else -> BatteryTheme.GoldenYellow
        }
        val metricColor = if (model.metricsStale) BatteryTheme.OnDarkSecondary else BatteryTheme.GoldenYellow
        val unitColor = if (model.metricsStale) BatteryTheme.OnDarkSecondary else BatteryTheme.OnDark
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                title = model.socLabel,
                value = formatMetric(model.socPercent, 1),
                unit = "%",
                modifier = Modifier.weight(1f),
                valueColor = socColor,
                unitColor = if (model.socIsLow && !model.metricsStale) socColor else unitColor,
                automationDescriptor = automation.soc,
            )
            MetricCard(
                title = model.voltageLabel,
                value = formatMetric(model.packVoltageV, 1),
                unit = "V",
                modifier = Modifier.weight(1f),
                valueColor = metricColor,
                unitColor = unitColor,
                automationDescriptor = automation.voltage,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                title = model.currentLabel,
                value = formatMetric(model.packCurrentA, 1),
                unit = "A",
                modifier = Modifier.weight(1f),
                valueColor = metricColor,
                unitColor = unitColor,
                automationDescriptor = automation.current,
            )
            MetricCard(
                title = model.powerLabel,
                value = formatMetric(model.powerKw, 2),
                unit = "kW",
                modifier = Modifier.weight(1f),
                valueColor = metricColor,
                unitColor = unitColor,
                automationDescriptor = automation.power,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                title = model.temperatureLabel,
                value = formatMetric(model.batteryTempAvgC, 1),
                unit = "°C",
                modifier = Modifier.weight(1f),
                valueColor = metricColor,
                unitColor = unitColor,
            )
            if (model.vehicleStatusTitle.isNotEmpty()) {
                MetricCard(
                    title = model.vehicleStatusTitle,
                    value = model.vehicleStatusLabel,
                    unit = "",
                    modifier = Modifier.weight(1f),
                    valueColor = metricColor,
                    unitColor = unitColor,
                )
            }
        }
        if (model.cloudStatusLabel.isNotEmpty()) {
            Text(
                text = model.cloudStatusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = BatteryTheme.OnDarkSecondary,
            )
        }
    }
}
