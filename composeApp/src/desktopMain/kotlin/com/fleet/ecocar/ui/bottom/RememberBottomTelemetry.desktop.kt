package com.fleet.ecocar.ui.bottom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberBottomTelemetry(): BottomTelemetry =
    remember { BottomTelemetry() }
