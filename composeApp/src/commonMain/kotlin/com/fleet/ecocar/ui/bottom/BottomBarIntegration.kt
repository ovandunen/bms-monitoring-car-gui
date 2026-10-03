package com.fleet.ecocar.ui.bottom

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

data class BottomBarIntegration(
    val telemetry: BottomTelemetry,
    val snackbarHostState: SnackbarHostState?,
)

@Composable
expect fun rememberBottomBarIntegration(): BottomBarIntegration
