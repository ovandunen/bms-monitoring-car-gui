package com.fleet.ecocar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecocar.gui.i18n.LanguageRepository
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.bottom.BottomTelemetry
import com.fleet.ecocar.ui.bottom.EcoBottomBar
import com.fleet.ecocar.ui.dialog.LastChanceBatteryDialog
import com.fleet.ecocar.ui.dialog.LowBatteryDialog
import com.fleet.ecocar.ui.dialog.OptimalSwapDialog
import com.fleet.ecocar.ui.main.MainContentArea
import com.fleet.ecocar.ui.side.EcoSideNav
import com.fleet.ecocar.ui.top.EcoTopBar
import com.fleet.ecocar.ui.top.TopBarMusicState

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    sidebarExpanded: Boolean,
    onSidebarToggle: () -> Unit,
    bottomExpanded: Boolean,
    onBottomToggle: () -> Unit,
    selected: MainDestination,
    onSelectDestination: (MainDestination) -> Unit,
    music: TopBarMusicState,
    telemetry: BottomTelemetry,
    ecoBmsTelemetry: EcoBmsTelemetry?,
    showLowBattery: Boolean,
    onDismissLowBattery: () -> Unit,
    showLastChance: Boolean,
    onDismissLastChance: () -> Unit,
    showOptimalSwap: Boolean,
    optimalSwapStationLabel: String,
    optimalSwapConfidencePercent: Int,
    onDismissOptimalSwap: () -> Unit,
    onNavigateToOptimalSwap: () -> Unit,
    onNavigateToCharging: () -> Unit,
    onTechnicalIssues: () -> Unit,
    onSimulateLowBattery: () -> Unit,
    onSimulateLastChance: () -> Unit = {},
    onBottomSettings: () -> Unit,
    onBottomInfo: () -> Unit,
    onTripLongPress: () -> Unit = {},
    showTripResetHint: Boolean = false,
    onTripResetHintDismissed: () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    languageRepository: LanguageRepository? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            EcoSideNav(
                expanded = sidebarExpanded,
                onToggleExpand = onSidebarToggle,
                selected = selected,
                onSelect = onSelectDestination,
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxSize(),
            ) {
                EcoTopBar(music = music)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(EcoCarColors.NearBlack),
                ) {
                    MainContentArea(
                        destination = selected,
                        ecoBmsTelemetry = ecoBmsTelemetry,
                        onSimulateLowBattery = onSimulateLowBattery,
                        onSimulateLastChance = onSimulateLastChance,
                        languageRepository = languageRepository,
                    )
                }
                EcoBottomBar(
                    expanded = bottomExpanded,
                    onToggleExpand = onBottomToggle,
                    telemetry = telemetry,
                    onSettingsClick = onBottomSettings,
                    onInfoClick = onBottomInfo,
                    onTripLongPress = onTripLongPress,
                    showTripResetHint = showTripResetHint,
                    onTripResetHintDismissed = onTripResetHintDismissed,
                )
            }
        }
        snackbarHostState?.let { hostState ->
            SnackbarHost(
                hostState = hostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp),
            )
        }
        if (showLastChance) {
            LastChanceBatteryDialog(
                onDismiss = onDismissLastChance,
                onNavigateToCharging = onNavigateToCharging,
                onTechnicalIssues = onTechnicalIssues,
            )
        } else if (showLowBattery) {
            LowBatteryDialog(
                onDismiss = onDismissLowBattery,
                onNavigateToCharging = onNavigateToCharging,
                onTechnicalIssues = onTechnicalIssues,
            )
        } else if (showOptimalSwap) {
            OptimalSwapDialog(
                stationLabel = optimalSwapStationLabel,
                confidencePercent = optimalSwapConfidencePercent,
                onDismiss = onDismissOptimalSwap,
                onNavigateToStation = onNavigateToOptimalSwap,
            )
        }
    }
}
