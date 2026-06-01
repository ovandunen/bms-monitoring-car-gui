package com.fleet.ecocar.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecocar.gui.nav.localizedLabel
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.placeholder_battery
import eco_car_gui.composeapp.generated.resources.placeholder_browser
import eco_car_gui.composeapp.generated.resources.placeholder_charts
import eco_car_gui.composeapp.generated.resources.placeholder_map
import eco_car_gui.composeapp.generated.resources.placeholder_music
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlaceholderScreen(destination: MainDestination) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = destination.localizedLabel(),
            style = MaterialTheme.typography.headlineMedium,
            color = EcoCarColors.OnDark,
        )
        Text(
            text = placeholderBlurb(destination),
            style = MaterialTheme.typography.bodyLarge,
            color = EcoCarColors.OnDarkSecondary,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun placeholderBlurb(d: MainDestination): String = stringResource(
    when (d) {
        MainDestination.Music -> Res.string.placeholder_music
        MainDestination.Map -> Res.string.placeholder_map
        MainDestination.Battery -> Res.string.placeholder_battery
        MainDestination.Charts -> Res.string.placeholder_charts
        MainDestination.Browser -> Res.string.placeholder_browser
        else -> Res.string.placeholder_music
    },
)
