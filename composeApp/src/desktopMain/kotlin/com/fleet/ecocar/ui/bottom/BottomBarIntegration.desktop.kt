package com.fleet.ecocar.ui.bottom

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberBottomBarIntegration(): BottomBarIntegration =
    BottomBarIntegration(
        telemetry = rememberBottomTelemetry(),
        showTripResetHint = false,
        onTripLongPress = {},
        onTripResetHintDismissed = {},
        snackbarHostState = null,
    )
