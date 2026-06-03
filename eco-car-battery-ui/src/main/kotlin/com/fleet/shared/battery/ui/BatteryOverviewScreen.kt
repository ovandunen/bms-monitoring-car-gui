package com.fleet.shared.battery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fleet.shared.battery.ui.internal.BatteryTheme
import com.fleet.shared.battery.ui.internal.MetricCard
import com.fleet.shared.battery.ui.internal.StatusHintRow
import com.fleet.shared.battery.ui.internal.formatMetric
import com.fleet.shared.battery.ui.internal.formatMetricHook

/**
 * Shared driver-facing battery overview (port). Stateless — callers supply [BatteryOverviewUiModel].
 */
@Composable
fun BatteryOverviewScreen(
    model: BatteryOverviewUiModel,
    onOpenSniffer: () -> Unit,
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
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val socValue = formatMetric(model.socPercent, 1)
            MetricCard(
                title = model.socLabel,
                value = socValue,
                unit = "%",
                modifier = Modifier.weight(1f),
                valueContentDescription = "battery-soc=${formatMetricHook(model.socPercent, 1)}",
            )
            MetricCard(
                title = model.voltageLabel,
                value = formatMetric(model.packVoltageV, 1),
                unit = "V",
                modifier = Modifier.weight(1f),
                valueContentDescription = "battery-voltage=${formatMetricHook(model.packVoltageV, 1)}",
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
                valueContentDescription = "battery-current=${formatMetricHook(model.packCurrentA, 1)}",
            )
            MetricCard(
                title = model.powerLabel,
                value = formatMetric(model.powerKw, 2),
                unit = "kW",
                modifier = Modifier.weight(1f),
                valueContentDescription = "battery-power=${formatMetricHook(model.powerKw, 2)}",
            )
        }
        Button(
            onClick = onOpenSniffer,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = BatteryTheme.GoldenYellow,
                contentColor = BatteryTheme.NearBlack,
            ),
        ) {
            Text(model.snifferButtonLabel)
        }
    }
}
