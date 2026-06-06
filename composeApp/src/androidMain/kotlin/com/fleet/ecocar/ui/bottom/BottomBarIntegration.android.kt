package com.fleet.ecocar.ui.bottom

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleet.ecocar.EcoCarApplication
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.bottom_trip_reset_snackbar
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun rememberBottomBarIntegration(): BottomBarIntegration {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val viewModel: BottomBarViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val application = app.applicationContext as Application
                return BottomBarViewModel(
                    batteryPort = app.batteryClient.asBottomBarBatteryPort(),
                    hintRepository = TripResetHintStore(application),
                ) as T
            }
        },
    )
    val telemetry by viewModel.telemetry.collectAsState()
    val showTripResetHint by viewModel.showTripResetHint.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resetMessage = stringResource(Res.string.bottom_trip_reset_snackbar)

    return BottomBarIntegration(
        telemetry = telemetry,
        showTripResetHint = showTripResetHint,
        onTripLongPress = {
            viewModel.resetTripDistance()
            scope.launch {
                snackbarHostState.showSnackbar(message = resetMessage)
            }
        },
        onTripResetHintDismissed = { viewModel.dismissTripResetHint() },
        snackbarHostState = snackbarHostState,
    )
}
