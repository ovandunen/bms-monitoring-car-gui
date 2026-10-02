package com.fleet.ecocar.ui.battery

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.battery_trip_running
import eco_car_gui.composeapp.generated.resources.trip_reset_button
import eco_car_gui.composeapp.generated.resources.trip_reset_confirm_body
import eco_car_gui.composeapp.generated.resources.trip_reset_confirm_title
import eco_car_gui.composeapp.generated.resources.trip_reset_no
import eco_car_gui.composeapp.generated.resources.trip_reset_yes
import org.jetbrains.compose.resources.stringResource
import java.text.DateFormat
import java.util.Date

@Composable
actual fun BatteryTripsContent(
    modifier: Modifier,
) {
    val viewModel: BatteryDashboardViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application,
        ),
    )
    LaunchedEffect(Unit) {
        viewModel.reloadTripSessions()
    }
    val sessions by viewModel.tripSessions.collectAsState()
    val running = stringResource(Res.string.battery_trip_running)
    val resetController = remember { TripResetController { viewModel.resetTrip() } }
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { resetController.onResetButtonClick() },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "trip-reset-button" },
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoCarColors.GoldenYellow,
                contentColor = EcoCarColors.NearBlack,
            ),
        ) {
            Text(stringResource(Res.string.trip_reset_button))
        }
        if (resetController.confirmationVisible) {
            AlertDialog(
                onDismissRequest = { resetController.onConfirmNo() },
                containerColor = EcoCarColors.SurfaceElevated,
                titleContentColor = EcoCarColors.OnDark,
                textContentColor = EcoCarColors.OnDarkSecondary,
                title = { Text(stringResource(Res.string.trip_reset_confirm_title)) },
                text = { Text(stringResource(Res.string.trip_reset_confirm_body)) },
                confirmButton = {
                    TextButton(onClick = { resetController.onConfirmYes() }) {
                        Text(stringResource(Res.string.trip_reset_yes), color = EcoCarColors.GoldenYellow)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resetController.onConfirmNo() }) {
                        Text(stringResource(Res.string.trip_reset_no), color = EcoCarColors.OnDarkSecondary)
                    }
                },
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(sessions, key = { it.id }) { session ->
                val row = TripRowFormatter.format(
                    startedAt = session.startedAt,
                    endedAt = session.endedAt,
                    distanceKm = session.distanceKm,
                    energyKwh = session.energyKwh,
                    runningLabel = running,
                )
                Card(colors = CardDefaults.cardColors(containerColor = EcoCarColors.SurfaceElevated)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = DateFormat.getDateTimeInstance().format(Date(row.startEpochMillis)),
                            color = EcoCarColors.OnDark,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = if (session.endedAt == TripRowFormatter.OPEN_ENDED_AT) {
                                row.endText
                            } else {
                                DateFormat.getDateTimeInstance().format(Date(session.endedAt))
                            },
                            color = EcoCarColors.OnDarkSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "${row.kmText} km · ${row.kwhText} kWh" +
                                (row.consumptionText?.let { " · $it kWh/km" } ?: ""),
                            color = EcoCarColors.GoldenYellow,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
