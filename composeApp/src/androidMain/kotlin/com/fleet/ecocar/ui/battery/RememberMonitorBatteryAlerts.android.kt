package com.fleet.ecocar.ui.battery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleet.shared.bms.ipc.domain.BatteryAlertNotification

@Composable
internal actual fun rememberMonitorBatteryAlerts(): List<DemoBatteryAlert> {
    val viewModel: BatteryDashboardViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application,
        ),
    )
    val alerts by viewModel.batteryAlerts.collectAsState()
    return alerts.map { it.toDemoBatteryAlert() }
}

private fun BatteryAlertNotification.toDemoBatteryAlert() = DemoBatteryAlert(
    severity = when (level) {
        3 -> DemoAlertSeverity.CRITICAL
        2 -> DemoAlertSeverity.WARNING
        else -> DemoAlertSeverity.INFO
    },
    message = message,
)
