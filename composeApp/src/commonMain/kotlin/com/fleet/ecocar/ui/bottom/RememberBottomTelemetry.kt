package com.fleet.ecocar.ui.bottom

import androidx.compose.runtime.Composable
import com.fleet.ecocar.ui.bottom.BottomTelemetry

/** VCU-backed bottom bar metrics on Android (range from BMS IPC); dashes on desktop until bound. */
@Composable
expect fun rememberBottomTelemetry(): BottomTelemetry
