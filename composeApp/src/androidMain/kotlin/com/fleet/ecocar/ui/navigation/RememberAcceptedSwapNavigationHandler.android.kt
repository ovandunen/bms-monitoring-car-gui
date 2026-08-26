package com.fleet.ecocar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication

@Composable
actual fun rememberAcceptedSwapNavigationHandler(): (String) -> Unit {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    return remember(app) {
        { stationId ->
            app.vehicleNavigation.navigateToAcceptedSwapStation(
                stationId,
                app.chargingStations.value,
            )
        }
    }
}
