package com.fleet.ecocar.map

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fleet.ecocar.domain.map.ChargingStation
import com.fleet.ecocar.infrastructure.map.MapViewWithStationPins
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.map_refresh
import eco_car_gui.composeapp.generated.resources.map_stations_empty
import eco_car_gui.composeapp.generated.resources.map_stations_legend
import eco_car_gui.composeapp.generated.resources.map_stations_loading
import eco_car_gui.composeapp.generated.resources.map_stations_title
import org.jetbrains.compose.resources.stringResource

private val PurpleUnknown = Color(0xFF9C27B0)
private val GreenAvailable = Color(0xFF4CAF50)

@Composable
actual fun EcoMapContent(
    modifier: Modifier,
    stations: List<EcoChargingStation>,
    isRefreshing: Boolean,
    onRefreshStations: () -> Unit,
) {
    val viewModel: EcoMapViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application,
        ),
    )
    val mapStations by viewModel.chargingStations.collectAsState()
    val mapRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshStations()
    }

    val mapStyleRepository = rememberMapStyleRepository()
    val styleUri = remember(mapStyleRepository) { mapStyleRepository.getStyleUrl() }

    var showMap by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        showMap = true
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.55f).fillMaxWidth()) {
            if (showMap) {
                MapViewWithStationPins(
                    styleUri = styleUri,
                    stations = mapStations,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(EcoCarColors.NearBlack),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = EcoCarColors.GoldenYellow,
                    )
                }
            }
            if (mapRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    color = EcoCarColors.GoldenYellow,
                    trackColor = EcoCarColors.SurfaceElevated,
                )
            }
            TextButton(
                onClick = { viewModel.refreshStations() },
                enabled = !mapRefreshing,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            ) {
                Text(stringResource(Res.string.map_refresh), color = EcoCarColors.GoldenYellow)
            }
        }
        Column(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxWidth()
                .background(EcoCarColors.SurfaceElevated)
                .padding(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.map_stations_title, mapStations.size),
                style = MaterialTheme.typography.titleMedium,
                color = EcoCarColors.OnDark,
            )
            Text(
                text = stringResource(Res.string.map_stations_legend),
                style = MaterialTheme.typography.labelSmall,
                color = EcoCarColors.OnDarkSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (mapRefreshing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = EcoCarColors.GoldenYellow,
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = stringResource(Res.string.map_stations_loading),
                        style = MaterialTheme.typography.bodySmall,
                        color = EcoCarColors.OnDarkSecondary,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            if (mapStations.isEmpty() && !mapRefreshing) {
                Text(
                    text = stringResource(Res.string.map_stations_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EcoCarColors.OnDarkSecondary,
                )
            } else if (mapStations.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(mapStations, key = { it.id }) { station ->
                        StationRow(station, stations)
                    }
                }
            }
        }
    }
}

@Composable
private fun StationRow(
    station: ChargingStation,
    ecoStations: List<EcoChargingStation>,
) {
    val eco = ecoStations.find { it.stationId == station.id }
    val dotColor = when {
        eco?.isPurpleUnknown == true -> PurpleUnknown
        eco?.status.equals("AVAILABLE", ignoreCase = true) -> GreenAvailable
        eco != null -> Color(0xFFFF9800)
        else -> GreenAvailable
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(station.name, color = EcoCarColors.OnDark, style = MaterialTheme.typography.bodyLarge)
            eco?.addressLine?.let {
                Text(it, color = EcoCarColors.OnDarkSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${"%.5f".format(station.latitude)}, ${"%.5f".format(station.longitude)}",
                color = EcoCarColors.OnDarkSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
