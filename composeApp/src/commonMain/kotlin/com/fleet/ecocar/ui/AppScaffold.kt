package com.fleet.ecocar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecocar.gui.i18n.LanguageRepository
import com.fleet.ecocar.browser.BrowserHomeStore
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.telemetry.EcoBmsAlert
import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.bottom.BottomTelemetry
import com.fleet.ecocar.ui.bottom.EcoBottomBar
import com.fleet.ecocar.ui.bottom.withSensorErrors
import com.fleet.ecocar.ui.dialog.InWindowDialogSlot
import com.fleet.ecocar.ui.dialog.LocalInWindowDialogSlot
import com.fleet.ecocar.ui.dialog.LowBatteryDialog
import com.fleet.ecocar.ui.main.MainContentArea
import com.fleet.ecocar.ui.side.EcoSideNav
import com.fleet.ecocar.ui.top.EcoTopBar
import com.fleet.ecocar.ui.top.MusicTransportHandlers
import com.fleet.ecocar.ui.top.TopBarMusicState
import com.fleet.ecocar.ui.volume.rememberMusicVolume

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    bottomExpanded: Boolean,
    onBottomToggle: () -> Unit,
    selected: MainDestination,
    onSelectDestination: (MainDestination) -> Unit,
    music: TopBarMusicState,
    transport: MusicTransportHandlers,
    telemetry: BottomTelemetry,
    ecoBmsTelemetry: EcoBmsTelemetry?,
    bmsAlerts: List<EcoBmsAlert> = emptyList(),
    showLowBattery: Boolean,
    onDismissLowBattery: () -> Unit,
    onNavigateToCharging: () -> Unit,
    onTechnicalIssues: () -> Unit,
    onSimulateLowBattery: () -> Unit,
    onBottomInfo: () -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    languageRepository: LanguageRepository? = null,
    browserHomeStore: BrowserHomeStore? = null,
    versionLabel: String = "",
    onToggleCanLog: (() -> Unit)? = null,
) {
    val volume = rememberMusicVolume()
    val dialogSlot = remember { InWindowDialogSlot() }
    CompositionLocalProvider(LocalInWindowDialogSlot provides dialogSlot) {
        Column(
            modifier = modifier.fillMaxSize().statusBarsPadding(),
        ) {
            EcoTopBar(music = music, transport = transport, volume = volume)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    EcoSideNav(
                        selected = selected,
                        onSelect = onSelectDestination,
                    )
                    Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(EcoCarColors.NearBlack),
                        ) {
                            MainContentArea(
                                destination = selected,
                                ecoBmsTelemetry = ecoBmsTelemetry,
                                bmsAlerts = bmsAlerts,
                                onSimulateLowBattery = onSimulateLowBattery,
                                languageRepository = languageRepository,
                                browserHomeStore = browserHomeStore,
                                versionLabel = versionLabel,
                                onToggleCanLog = onToggleCanLog,
                            )
                            snackbarHostState?.let { hostState ->
                                SnackbarHost(
                                    hostState = hostState,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp),
                                )
                            }
                        }
                        EcoBottomBar(
                            expanded = bottomExpanded,
                            onToggleExpand = onBottomToggle,
                            telemetry = telemetry.withSensorErrors(ecoBmsTelemetry),
                            onInfoClick = onBottomInfo,
                        )
                    }
                }
                dialogSlot.content.value?.invoke()
                if (showLowBattery) {
                    LowBatteryDialog(
                        onDismiss = onDismissLowBattery,
                        onNavigateToCharging = onNavigateToCharging,
                        onTechnicalIssues = onTechnicalIssues,
                    )
                }
            }
        }
    }
}
