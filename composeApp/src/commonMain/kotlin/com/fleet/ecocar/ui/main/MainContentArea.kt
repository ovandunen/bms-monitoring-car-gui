package com.fleet.ecocar.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecocar.gui.i18n.LanguageRepository
import com.ecocar.gui.ui.settings.LanguageSettingsSection
import com.fleet.ecocar.browser.EcoBrowserContent
import com.fleet.ecocar.music.EcoMusicContent
import com.fleet.ecocar.map.EcoMapContent
import com.fleet.ecocar.map.rememberChargingStationMapState
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import com.fleet.ecocar.ui.battery.BatterySubNav
import com.fleet.ecocar.ui.charts.ChartsSubNav
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.settings_blurb
import eco_car_gui.composeapp.generated.resources.settings_test_low_battery
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainContentArea(
    destination: MainDestination,
    ecoBmsTelemetry: EcoBmsTelemetry?,
    onSimulateLowBattery: () -> Unit,
    onOpenSniffer: () -> Unit = {},
    languageRepository: LanguageRepository? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
    ) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when (destination) {
                MainDestination.Music ->
                    EcoMusicContent(Modifier.fillMaxSize())
                MainDestination.Battery ->
                    BatterySubNav(
                        onOpenSniffer = onOpenSniffer,
                        bmsTelemetry = ecoBmsTelemetry,
                        modifier = Modifier.fillMaxSize(),
                    )
                MainDestination.Map -> {
                    val mapState = rememberChargingStationMapState()
                    EcoMapContent(
                        modifier = Modifier.fillMaxSize(),
                        stations = mapState.stations,
                        onRefreshStations = mapState.refresh,
                    )
                }
                MainDestination.Browser ->
                    EcoBrowserContent(Modifier.fillMaxSize())
                MainDestination.Charts ->
                    ChartsSubNav(
                        modifier = Modifier.fillMaxSize(),
                        bmsTelemetry = ecoBmsTelemetry,
                    )
                MainDestination.Settings ->
                    if (languageRepository != null) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            Text(
                                text = stringResource(Res.string.settings_blurb),
                                style = MaterialTheme.typography.bodyMedium,
                                color = EcoCarColors.OnDarkSecondary,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                            LanguageSettingsSection(languageRepository = languageRepository)
                        }
                    } else {
                        PlaceholderScreen(destination = destination)
                    }
                else ->
                    PlaceholderScreen(destination = destination)
            }
        }
        if (destination == MainDestination.Settings) {
            Button(
                onClick = onSimulateLowBattery,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoCarColors.GoldenYellow,
                    contentColor = EcoCarColors.NearBlack,
                ),
            ) {
                Text(
                    text = stringResource(Res.string.settings_test_low_battery),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
