package com.fleet.ecocar.ui.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.shared.battery.ui.BatteryOverviewScreen
import com.fleet.shared.bms.ipc.domain.ConnectionStatus
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.battery_bms_offline
import eco_car_gui.composeapp.generated.resources.battery_connecting
import eco_car_gui.composeapp.generated.resources.battery_demo_hint
import eco_car_gui.composeapp.generated.resources.battery_live_hint
import eco_car_gui.composeapp.generated.resources.battery_sniffer_btn
import eco_car_gui.composeapp.generated.resources.battery_title
import eco_car_gui.composeapp.generated.resources.battery_wake_bms
import eco_car_gui.composeapp.generated.resources.metric_pack_current
import eco_car_gui.composeapp.generated.resources.metric_pack_voltage
import eco_car_gui.composeapp.generated.resources.metric_power
import eco_car_gui.composeapp.generated.resources.metric_soc
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BatteryDashboardOverview(
    onOpenSniffer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BatteryDashboardViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application,
        ),
    ),
) {
    val batteryState by viewModel.batteryState.collectAsState()
    val status by viewModel.connectionStatus.collectAsState()

    val labels = BatteryOverviewLabels(
        screenTitle = stringResource(Res.string.battery_title),
        socLabel = stringResource(Res.string.metric_soc),
        voltageLabel = stringResource(Res.string.metric_pack_voltage),
        currentLabel = stringResource(Res.string.metric_pack_current),
        powerLabel = stringResource(Res.string.metric_power),
        liveHint = stringResource(Res.string.battery_live_hint),
        demoHint = stringResource(Res.string.battery_demo_hint),
        connectingHint = stringResource(Res.string.battery_connecting),
        offlineHint = stringResource(Res.string.battery_bms_offline),
        snifferLabel = stringResource(Res.string.battery_sniffer_btn),
    )

    Box(modifier = modifier.fillMaxSize()) {
        when (val s = status) {
            is ConnectionStatus.Connected -> {
                batteryState?.let { snap ->
                    BatteryOverviewScreen(
                        model = snap.toOverviewUiModel(s, labels),
                        onOpenSniffer = onOpenSniffer,
                        modifier = Modifier.fillMaxSize(),
                    )
                } ?: ConnectingPanel(stringResource(Res.string.battery_connecting))
            }
            is ConnectionStatus.Connecting -> {
                ConnectingPanel(stringResource(Res.string.battery_connecting))
            }
            is ConnectionStatus.BmsOffline -> {
                OfflinePanel(
                    lastSeen = s.lastSeenTimestamp,
                    onWakeBms = { viewModel.wakeBms() },
                )
            }
            is ConnectionStatus.Error -> {
                ErrorPanel(s.reason, onRetry = { viewModel.wakeBms() })
            }
            ConnectionStatus.Disconnected -> {
                OfflinePanel(lastSeen = null, onWakeBms = { viewModel.wakeBms() })
            }
        }
    }
}

@Composable
private fun ConnectingPanel(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = EcoCarColors.GoldenYellow)
        Text(
            text = message,
            modifier = Modifier.padding(top = 12.dp),
            color = EcoCarColors.OnDarkSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun OfflinePanel(lastSeen: Long?, onWakeBms: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.battery_bms_offline),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
        )
        if (lastSeen != null && lastSeen > 0L) {
            Text(
                text = "Last seen: $lastSeen",
                modifier = Modifier.padding(top = 8.dp),
                color = EcoCarColors.OnDarkSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Button(
            onClick = onWakeBms,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoCarColors.GoldenYellow,
                contentColor = EcoCarColors.NearBlack,
            ),
        ) {
            Text(stringResource(Res.string.battery_wake_bms))
        }
    }
}

@Composable
private fun ErrorPanel(reason: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Error: $reason",
            color = Color(0xFFFF6B6B),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoCarColors.GoldenYellow,
                contentColor = EcoCarColors.NearBlack,
            ),
        ) {
            Text(stringResource(Res.string.battery_wake_bms))
        }
    }
}
