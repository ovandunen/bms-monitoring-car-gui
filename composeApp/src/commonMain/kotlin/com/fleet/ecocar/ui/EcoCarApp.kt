package com.fleet.ecocar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.ecocar.gui.i18n.AppLocaleEnvironment
import com.ecocar.gui.i18n.LanguageRepository
import com.ecocar.gui.i18n.collectLanguageAsState
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.telemetry.rememberEcoBmsTelemetry
import com.fleet.ecocar.ui.bottom.rememberBottomBarIntegration
import com.fleet.ecocar.ui.vehicle.ObserveVcuLowBattery
import com.fleet.ecocar.ui.theme.EcoCarTheme
import com.fleet.ecocar.ui.top.rememberLiveMusicTopBarState

@Composable
fun EcoCarApp(
    languageRepository: LanguageRepository,
) {
    val currentLang = languageRepository.selectedLanguage.collectLanguageAsState(
        languageRepository.getDefault(),
    )

    AppLocaleEnvironment(currentLang.value) {
        EcoCarTheme {
            var sidebarExpanded by remember { mutableStateOf(true) }
            var bottomExpanded by remember { mutableStateOf(true) }
            var selected by remember { mutableStateOf(MainDestination.Battery) }
            var showLowBattery by remember { mutableStateOf(false) }

            val music = rememberLiveMusicTopBarState()
            val bottomBar = rememberBottomBarIntegration()
            val ecoBmsTelemetry = rememberEcoBmsTelemetry()

            ObserveVcuLowBattery { showLowBattery = true }

            Box(Modifier.fillMaxSize()) {
                AppScaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .onPreviewKeyEvent { ev ->
                            if (ev.type == KeyEventType.KeyDown && ev.key == Key.F9) {
                                showLowBattery = true
                                true
                            } else {
                                false
                            }
                        },
                    sidebarExpanded = sidebarExpanded,
                    onSidebarToggle = { sidebarExpanded = !sidebarExpanded },
                    bottomExpanded = bottomExpanded,
                    onBottomToggle = { bottomExpanded = !bottomExpanded },
                    selected = selected,
                    onSelectDestination = { selected = it },
                    music = music,
                    telemetry = bottomBar.telemetry,
                    ecoBmsTelemetry = ecoBmsTelemetry,
                    showLowBattery = showLowBattery,
                    onDismissLowBattery = { showLowBattery = false },
                    onNavigateToCharging = { selected = MainDestination.Map },
                    onTechnicalIssues = { /* v1: Hook für Diagnose */ },
                    onSimulateLowBattery = { showLowBattery = true },
                    onBottomSettings = { selected = MainDestination.Settings },
                    onBottomInfo = { /* v1: Info-Panel */ },
                    onTripLongPress = bottomBar.onTripLongPress,
                    showTripResetHint = bottomBar.showTripResetHint,
                    onTripResetHintDismissed = bottomBar.onTripResetHintDismissed,
                    snackbarHostState = bottomBar.snackbarHostState,
                    languageRepository = languageRepository,
                )
            }
        }
    }
}
