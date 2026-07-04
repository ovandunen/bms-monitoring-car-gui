package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable

/**
 * When VCU SOC crosses Meldungs thresholds, invoke callbacks once per episode:
 * - Stufe 2: SOC ≤ [LOW_BATTERY_PERCENT] (20 %)
 * - Stufe 3: SOC ≤ [LAST_CHANCE_BATTERY_PERCENT] (5 %), takes priority over Stufe 2
 */
@Composable
expect fun ObserveVcuBatteryAlerts(
    onLowBattery: () -> Unit,
    onLastChance: () -> Unit,
)
