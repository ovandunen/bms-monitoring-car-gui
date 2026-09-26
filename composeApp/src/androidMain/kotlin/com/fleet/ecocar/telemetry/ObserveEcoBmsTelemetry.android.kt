package com.fleet.ecocar.telemetry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication

@Composable
actual fun rememberEcoBmsTelemetry(): EcoBmsTelemetry? {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val v by app.ecoBmsTelemetry.collectAsState(initial = null)
    return v
}

@Composable
actual fun rememberBmsAlerts(): List<EcoBmsAlert> {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val v by app.bmsAlerts.collectAsState(initial = emptyList())
    return v
}
