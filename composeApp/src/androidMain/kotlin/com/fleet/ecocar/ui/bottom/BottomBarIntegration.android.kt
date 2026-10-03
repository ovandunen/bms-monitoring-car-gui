package com.fleet.ecocar.ui.bottom

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleet.ecocar.EcoCarApplication

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

    return BottomBarIntegration(
        telemetry = telemetry,
        snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() },
    )
}
