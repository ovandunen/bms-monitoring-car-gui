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
import com.fleet.ecocar.ui.navigation.rememberAcceptedSwapNavigationHandler
import com.fleet.ecocar.ui.vehicle.ObserveCsmsSwapRecommendation
import com.fleet.ecocar.ui.vehicle.ObserveVcuBatteryAlerts
import com.fleet.ecocar.ui.vehicle.rememberSwapFeedbackPublisher
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
            var showLastChance by remember { mutableStateOf(false) }
            var showOptimalSwap by remember { mutableStateOf(false) }
            var optimalSwapCorrelationId by remember { mutableStateOf("") }
            var optimalSwapStationId by remember { mutableStateOf("") }
            var optimalSwapStationLabel by remember { mutableStateOf("") }
            var optimalSwapConfidence by remember { mutableStateOf(0) }

            val music = rememberLiveMusicTopBarState()
            val bottomBar = rememberBottomBarIntegration()
            val publishSwapFeedback = rememberSwapFeedbackPublisher()
            val ecoBmsTelemetry = rememberEcoBmsTelemetry()
            val onAcceptedSwapNavigation = rememberAcceptedSwapNavigationHandler()

            ObserveVcuBatteryAlerts(
                onLowBattery = { showLowBattery = true },
                onLastChance = {
                    showLastChance = true
                    showLowBattery = false
                },
            )

            ObserveCsmsSwapRecommendation { correlationId, stationId, stationLabel, confidencePercent ->
                optimalSwapCorrelationId = correlationId
                optimalSwapStationId = stationId
                optimalSwapStationLabel = stationLabel
                optimalSwapConfidence = confidencePercent
                showOptimalSwap = true
            }

            Box(Modifier.fillMaxSize()) {
                AppScaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .onPreviewKeyEvent { ev ->
                            if (ev.type == KeyEventType.KeyDown && ev.key == Key.F9) {
                                showLowBattery = true
                                showLastChance = false
                                true
                            } else if (ev.type == KeyEventType.KeyDown && ev.key == Key.F10) {
                                showLastChance = true
                                showLowBattery = false
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
                    showLastChance = showLastChance,
                    onDismissLastChance = { showLastChance = false },
                    showOptimalSwap = showOptimalSwap,
                    optimalSwapStationLabel = optimalSwapStationLabel,
                    optimalSwapConfidencePercent = optimalSwapConfidence,
                    onDismissOptimalSwap = {
                        showOptimalSwap = false
                        publishSwapFeedback(optimalSwapCorrelationId, "dismissed", optimalSwapStationId)
                    },
                    onNavigateToOptimalSwap = {
                        publishSwapFeedback(optimalSwapCorrelationId, "accepted", optimalSwapStationId)
                        showOptimalSwap = false
                        selected = MainDestination.Map
                        onAcceptedSwapNavigation(optimalSwapStationId)
                    },
                    onNavigateToCharging = { selected = MainDestination.Map },
                    onTechnicalIssues = { /* v1: Hook für Diagnose */ },
                    onSimulateLowBattery = {
                        showLowBattery = true
                        showLastChance = false
                    },
                    onSimulateLastChance = {
                        showLastChance = true
                        showLowBattery = false
                    },
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
