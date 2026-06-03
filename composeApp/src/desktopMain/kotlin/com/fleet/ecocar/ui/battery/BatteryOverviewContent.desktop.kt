package com.fleet.ecocar.ui.battery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
actual fun BatteryOverviewContent(
    onOpenSniffer: () -> Unit,
    modifier: Modifier,
) {
    var snapshot by remember { mutableStateOf(DemoBatterySnapshot.initial()) }
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(900L)
            snapshot = snapshot.evolve()
        }
    }
    BatteryOverviewTab(
        snapshot = snapshot,
        bmsActive = false,
        onOpenSniffer = onOpenSniffer,
        modifier = modifier,
    )
}
