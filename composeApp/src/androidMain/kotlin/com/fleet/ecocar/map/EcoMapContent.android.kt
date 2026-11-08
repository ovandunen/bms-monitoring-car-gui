package com.fleet.ecocar.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.fleet.ecocar.domain.map.toMapPin
import com.fleet.ecocar.domain.navigation.RoutePhase
import com.fleet.ecocar.infrastructure.map.MapRouteOverlay
import com.fleet.ecocar.infrastructure.map.MapViewWithStationPins
import com.fleet.ecocar.infrastructure.map.MapZoomPort
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverTouch
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.map_refresh
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** Map share of this column was 0.55. Factor 1.15 makes it 0.6325; the list keeps the rest. */
private const val MAP_HEIGHT_WEIGHT = 0.55f * 1.15f
private const val STATIONS_HEIGHT_WEIGHT = 1f - MAP_HEIGHT_WEIGHT

@Composable
actual fun EcoMapContent(
    modifier: Modifier,
    stations: List<EcoChargingStation>,
    isRefreshing: Boolean,
    onRefreshStations: () -> Unit,
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
    val navigation = rememberRouteNavigation()
    val phase by navigation.phase.collectAsState()
    val selected by navigation.selected.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(vehicleLatitude, vehicleLongitude, vehicleHeadingDegrees) {
        navigation.updateVehicle(vehicleLatitude, vehicleLongitude, vehicleHeadingDegrees)
    }

    var showMap by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        showMap = true
    }

    val emptyStations = EcoMapStationPresenter.shouldShowEmptyState(stations, isRefreshing)
    val routePoints = (phase as? RoutePhase.Active)?.route?.shape ?: emptyList()

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
                    route = MapRouteOverlay(routePoints) { id ->
                        stations.find { it.stationId == id }?.toMapPin()?.let(navigation::select)
                    },
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(EcoCarColors.NearBlack),
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
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
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
            MapNavigationOverlay(
                featureEnabled = navigation.featureEnabled,
                selectedName = selected?.name,
                phase = phase,
                onStart = { scope.launch { navigation.start() } },
                onEnd = navigation::end,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = DriverTouch.ScreenEdge,
                        end = DriverTouch.MinTarget + DriverTouch.ScreenEdge,
                        bottom = DriverTouch.ScreenEdge,
                    ),
            )
        }
        MapStationList(
            stations = stations,
            isRefreshing = isRefreshing,
            empty = emptyStations,
            modifier = if (emptyStations) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.weight(STATIONS_HEIGHT_WEIGHT).fillMaxWidth()
            },
        )
    }
}
