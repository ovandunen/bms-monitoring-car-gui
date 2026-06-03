package com.ecocar.gui.nav

import androidx.compose.runtime.Composable
import com.fleet.ecocar.nav.MainDestination
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.nav_battery
import eco_car_gui.composeapp.generated.resources.nav_browser
import eco_car_gui.composeapp.generated.resources.nav_charts
import eco_car_gui.composeapp.generated.resources.nav_map
import eco_car_gui.composeapp.generated.resources.nav_music
import eco_car_gui.composeapp.generated.resources.nav_settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainDestination.localizedLabel(): String = stringResource(
    when (this) {
        MainDestination.Music -> Res.string.nav_music
        MainDestination.Map -> Res.string.nav_map
        MainDestination.Battery -> Res.string.nav_battery
        MainDestination.Charts -> Res.string.nav_charts
        MainDestination.Browser -> Res.string.nav_browser
        MainDestination.Settings -> Res.string.nav_settings
    },
)
