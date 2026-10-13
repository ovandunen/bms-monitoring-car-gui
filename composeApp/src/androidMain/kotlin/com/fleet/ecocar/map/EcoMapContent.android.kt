package com.fleet.ecocar.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.infrastructure.map.MapViewWithStationPins
import com.fleet.ecocar.infrastructure.map.MapZoomPort
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverIconButton
import com.fleet.ecocar.ui.DriverTouch
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.map_refresh
import eco_car_gui.composeapp.generated.resources.map_stations_empty_line
import eco_car_gui.composeapp.generated.resources.map_stations_legend
import eco_car_gui.composeapp.generated.resources.map_stations_loading
import eco_car_gui.composeapp.generated.resources.map_stations_title
import eco_car_gui.composeapp.generated.resources.map_zoom_in
import eco_car_gui.composeapp.generated.resources.map_zoom_out
import org.jetbrains.compose.resources.stringResource

private val PurpleUnknown = Color(0xFF9C27B0)
private val GreenAvailable = Color(0xFF4CAF50)

/** Map share of this column was 0.55. Factor 1.15 makes it 0.6325; the list keeps the rest. */
private const val MAP_HEIGHT_WEIGHT = 0.55f * 1.15f
private const val STATIONS_HEIGHT_WEIGHT = 1f - MAP_HEIGHT_WEIGHT

@Composable
actual fun EcoMapContent(
    modifier: Modifier,
    stations: List<EcoChargingStation>,
    isRefreshing: Boolean,
    onRefreshStations: () -> Unit,
    // ADDED to match the commonMain expect declaration, now passed
    // straight through to MapViewWithStationPins below.
    vehicleLatitude: Double?,
    vehicleLongitude: Double?,
    vehicleHeadingDegrees: Double?,
) {
    LaunchedEffect(Unit) {
        onRefreshStations()
    }

    val pinStations = remember(stations) { EcoMapStationPresenter.mapPins(stations) }
    val mapStyleRepository = rememberMapStyleRepository()
    val styleUri = remember(mapStyleRepository) { mapStyleRepository.getStyleUrl() }
    val zoomPort = remember { MapZoomPort() }

    var showMap by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        showMap = true
    }

    val emptyStations = EcoMapStationPresenter.shouldShowEmptyState(stations, isRefreshing)

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(if (emptyStations) 1f else MAP_HEIGHT_WEIGHT)
                .fillMaxWidth(),
        ) {
            if (showMap) {
                MapViewWithStationPins(
                    styleUri = styleUri,
                    stations = pinStations,
                    vehicleLatitude = vehicleLatitude,
                    vehicleLongitude = vehicleLongitude,
                    vehicleHeadingDegrees = vehicleHeadingDegrees,
                    zoomPort = zoomPort,
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
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    color = EcoCarColors.GoldenYellow,
                    trackColor = EcoCarColors.SurfaceElevated,
                )
            }
            TextButton(
                onClick = onRefreshStations,
                enabled = !isRefreshing,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            ) {
                Text(stringResource(Res.string.map_refresh), color = EcoCarColors.GoldenYellow)
            }
            MapZoomButtons(
                zoomIn = zoomPort::zoomIn,
                zoomOut = zoomPort::zoomOut,
                enabled = zoomPort.ready,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
        if (emptyStations) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EcoCarColors.SurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(
                        Res.string.map_stations_title,
                        EcoMapStationPresenter.stationCount(stations),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = EcoCarColors.OnDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(Res.string.map_stations_empty_line),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EcoCarColors.OnDarkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (!emptyStations) {
        Column(
            modifier = Modifier
                .weight(STATIONS_HEIGHT_WEIGHT)
                .fillMaxWidth()
                .background(EcoCarColors.SurfaceElevated)
                .padding(12.dp),
        ) {
            Text(
                text = stringResource(
                    Res.string.map_stations_title,
                    EcoMapStationPresenter.stationCount(stations),
                ),
                style = MaterialTheme.typography.titleMedium,
                color = EcoCarColors.OnDark,
            )
            Text(
                text = stringResource(Res.string.map_stations_legend),
                style = MaterialTheme.typography.labelSmall,
                color = EcoCarColors.OnDarkSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (isRefreshing) {
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
            if (EcoMapStationPresenter.shouldShowStationList(stations)) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(stations, key = { it.stationId }) { station ->
                        StationRow(station)
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun MapZoomButtons(
    zoomIn: () -> Unit,
    zoomOut: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(DriverTouch.ScreenEdge),
        verticalArrangement = Arrangement.spacedBy(DriverTouch.Gap),
    ) {
        DriverIconButton(
            onClick = zoomIn,
            contentDescription = stringResource(Res.string.map_zoom_in),
            icon = Icons.Filled.Add,
            size = DriverTouch.MinTarget,
            enabled = enabled,
            restingTint = EcoCarColors.OnDark,
            containerColor = EcoCarColors.NearBlack.copy(alpha = 0.72f),
        )
        DriverIconButton(
            onClick = zoomOut,
            contentDescription = stringResource(Res.string.map_zoom_out),
            icon = Icons.Filled.Remove,
            size = DriverTouch.MinTarget,
            enabled = enabled,
            restingTint = EcoCarColors.OnDark,
            containerColor = EcoCarColors.NearBlack.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun StationRow(station: EcoChargingStation) {
    val dotColor = when {
        station.isPurpleUnknown -> PurpleUnknown
        station.status.equals("AVAILABLE", ignoreCase = true) -> GreenAvailable
        else -> Color(0xFFFF9800)
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
            Text(station.displayName, color = EcoCarColors.OnDark, style = MaterialTheme.typography.bodyLarge)
            station.addressLine?.let {
                Text(it, color = EcoCarColors.OnDarkSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${"%.5f".format(station.latitude)}, ${"%.5f".format(station.longitude)} · ${station.solarCapacityKw.toInt()} kW",
                color = EcoCarColors.OnDarkSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}