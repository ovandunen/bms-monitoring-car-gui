package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberSwapFeedbackPublisher(): (
    correlationId: String,
    state: String,
    stationId: String?,
) -> Unit = remember { { _, _, _ -> } }
