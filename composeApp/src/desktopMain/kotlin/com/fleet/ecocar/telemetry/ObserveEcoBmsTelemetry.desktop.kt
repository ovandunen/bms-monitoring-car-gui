package com.fleet.ecocar.telemetry

import androidx.compose.runtime.Composable

@Composable
actual fun rememberEcoBmsTelemetry(): EcoBmsTelemetry? = null

@Composable
actual fun rememberBmsAlerts(): List<EcoBmsAlert> = emptyList()
