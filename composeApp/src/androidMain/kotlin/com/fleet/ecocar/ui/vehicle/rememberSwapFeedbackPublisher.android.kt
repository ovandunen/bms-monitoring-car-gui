package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication

@Composable
actual fun rememberSwapFeedbackPublisher(): (
    correlationId: String,
    state: String,
    stationId: String?,
) -> Unit {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    return remember(app) { { correlationId, state, stationId ->
        app.publishSwapFeedback(correlationId, state, stationId)
    } }
}
