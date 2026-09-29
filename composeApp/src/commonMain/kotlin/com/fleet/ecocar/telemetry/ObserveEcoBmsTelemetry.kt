package com.fleet.ecocar.telemetry

import androidx.compose.runtime.Composable

/** Letzter BMS-Snapshot aus der Android IPC-Schicht; Desktop: immer `null`. */
@Composable
expect fun rememberEcoBmsTelemetry(): EcoBmsTelemetry?

/** Family-B Alerts aus der Android IPC-Schicht; Desktop: leer. */
@Composable
expect fun rememberBmsAlerts(): List<EcoBmsAlert>
