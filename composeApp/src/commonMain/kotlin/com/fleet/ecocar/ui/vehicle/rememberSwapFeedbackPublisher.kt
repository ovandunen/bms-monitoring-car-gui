package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable

@Composable
expect fun rememberSwapFeedbackPublisher(): (
    correlationId: String,
    state: String,
    stationId: String?,
) -> Unit
