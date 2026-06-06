package com.fleet.ecocar.ui.battery

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun BatteryOverviewContent(
    modifier: Modifier,
) {
    BatteryOverviewTab(
        metrics = BatteryOverviewMetrics.Empty,
        bmsActive = false,
        modifier = modifier,
    )
}
