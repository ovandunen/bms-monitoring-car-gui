package com.fleet.ecocar.ui.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.battery_trip_running
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
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
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
