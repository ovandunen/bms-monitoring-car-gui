package com.fleet.ecocar.ui.battery

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BatteryOverviewContent(
    onOpenSniffer: () -> Unit,
    modifier: Modifier = Modifier,
)
