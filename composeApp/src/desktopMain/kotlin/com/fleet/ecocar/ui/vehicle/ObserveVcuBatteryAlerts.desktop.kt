package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable

@Composable
actual fun ObserveVcuBatteryAlerts(
    onLowBattery: () -> Unit,
    onLastChance: () -> Unit,
) = Unit
