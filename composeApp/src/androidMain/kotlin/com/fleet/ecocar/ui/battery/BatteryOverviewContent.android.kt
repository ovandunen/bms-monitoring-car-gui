package com.fleet.ecocar.ui.battery

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun BatteryOverviewContent(
    onOpenSniffer: () -> Unit,
    modifier: Modifier,
) {
    BatteryDashboardOverview(
        onOpenSniffer = onOpenSniffer,
        modifier = modifier,
    )
}
