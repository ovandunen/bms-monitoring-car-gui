package com.fleet.ecocar.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.fleet.ecocar.ui.bottom.BottomTelemetry
import com.fleet.ecocar.ui.theme.EcoCarTheme
import com.fleet.ecocar.ui.top.rememberLiveMusicTopBarState
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.action_ok
import eco_car_gui.composeapp.generated.resources.sniffer_body
import eco_car_gui.composeapp.generated.resources.sniffer_title
import org.jetbrains.compose.resources.stringResource

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
            var showSnifferDemo by remember { mutableStateOf(false) }

            val music = rememberLiveMusicTopBarState()
            val telemetry = remember { BottomTelemetry() }
            val ecoBmsTelemetry = rememberEcoBmsTelemetry()

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
                    telemetry = telemetry,
                    ecoBmsTelemetry = ecoBmsTelemetry,
                    showLowBattery = showLowBattery,
                    onDismissLowBattery = { showLowBattery = false },
                    onNavigateToCharging = { selected = MainDestination.Map },
                    onTechnicalIssues = { /* v1: Hook für Diagnose */ },
                    onSimulateLowBattery = { showLowBattery = true },
                    onBottomSettings = { selected = MainDestination.Settings },
                    onBottomInfo = { /* v1: Info-Panel */ },
                    onOpenSniffer = { showSnifferDemo = true },
                    languageRepository = languageRepository,
                )
                if (showSnifferDemo) {
                    AlertDialog(
                        onDismissRequest = { showSnifferDemo = false },
                        title = { Text(stringResource(Res.string.sniffer_title)) },
                        text = { Text(stringResource(Res.string.sniffer_body)) },
                        confirmButton = {
                            TextButton(onClick = { showSnifferDemo = false }) {
                                Text(stringResource(Res.string.action_ok))
                            }
                        },
                    )
                }
            }
        }
    }
}
