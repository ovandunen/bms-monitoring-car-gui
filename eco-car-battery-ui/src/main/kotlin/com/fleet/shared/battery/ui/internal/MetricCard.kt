package com.fleet.shared.battery.ui.internal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun MetricCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    valueColor: Color = BatteryTheme.GoldenYellow,
    unitColor: Color = BatteryTheme.OnDark,
    /** Exposed to uiautomator for integration tests (e.g. `battery-soc=12.0`). */
    automationDescriptor: String? = null,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BatteryTheme.SurfaceElevated),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = BatteryTheme.OnDarkSecondary,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .then(
                        if (automationDescriptor != null) {
                            Modifier.semantics(mergeDescendants = true) {
                                contentDescription = automationDescriptor
                            }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                )
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = unitColor,
                )
            }
        }
    }
}

internal fun formatMetric(value: Float?, decimals: Int): String =
    com.fleet.shared.battery.ui.application.BatteryOverviewAutomationDescriptors
        .formatMetric(value, decimals)
