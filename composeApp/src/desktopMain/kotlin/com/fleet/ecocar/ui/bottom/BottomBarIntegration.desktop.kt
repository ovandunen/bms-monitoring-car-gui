package com.fleet.ecocar.ui.bottom

import androidx.compose.runtime.Composable

@Composable
actual fun rememberBottomBarIntegration(): BottomBarIntegration =
    BottomBarIntegration(
        telemetry = rememberBottomTelemetry(),
        snackbarHostState = null,
    )
