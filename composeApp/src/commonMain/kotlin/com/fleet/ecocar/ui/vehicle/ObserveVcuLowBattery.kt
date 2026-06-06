package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable

/**
 * When VCU SOC drops below [com.fleet.ecocar.domain.vehicle.LadestationSocPolicy.LOW_BATTERY_PERCENT],
 * invoke [onLowBattery] once per episode (same rule BMS uses for Ladestation preload).
 */
@Composable
expect fun ObserveVcuLowBattery(onLowBattery: () -> Unit)
