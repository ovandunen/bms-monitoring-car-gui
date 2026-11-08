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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverIconButton
import com.fleet.ecocar.ui.DriverTouch
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.map_stations_empty_line
import eco_car_gui.composeapp.generated.resources.map_stations_legend
import eco_car_gui.composeapp.generated.resources.map_stations_loading
import eco_car_gui.composeapp.generated.resources.map_stations_title
import eco_car_gui.composeapp.generated.resources.map_zoom_in
import eco_car_gui.composeapp.generated.resources.map_zoom_out
import org.jetbrains.compose.resources.stringResource

private val PurpleUnknown = Color(0xFF9C27B0)
private val GreenAvailable = Color(0xFF4CAF50)

@Composable
fun MapZoomButtons(
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
fun MapStationList(
    stations: List<EcoChargingStation>,
    isRefreshing: Boolean,
    empty: Boolean,
    modifier: Modifier = Modifier,
) {
    if (empty) {
        Column(
            modifier = modifier
                .background(EcoCarColors.SurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.map_stations_title, EcoMapStationPresenter.stationCount(stations)),
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
        return
    }
    Column(
        modifier = modifier
            .background(EcoCarColors.SurfaceElevated)
            .padding(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.map_stations_title, EcoMapStationPresenter.stationCount(stations)),
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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

@Composable
private fun StationRow(station: EcoChargingStation) {
    val dotColor = when {
        station.isPurpleUnknown -> PurpleUnknown
        station.status.equals("AVAILABLE", ignoreCase = true) -> GreenAvailable
        else -> Color(0xFFFF9800)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(12.dp).clip(CircleShape).background(dotColor),
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
