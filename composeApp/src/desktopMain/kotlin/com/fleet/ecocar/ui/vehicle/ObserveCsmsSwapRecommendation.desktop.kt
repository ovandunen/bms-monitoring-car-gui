package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable

@Composable
actual fun ObserveCsmsSwapRecommendation(
    onOptimalSwap: (
        correlationId: String,
        stationId: String,
        stationLabel: String,
        confidencePercent: Int,
    ) -> Unit,
) = Unit
