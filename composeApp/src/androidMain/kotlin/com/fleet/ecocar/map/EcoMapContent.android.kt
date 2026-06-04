package com.fleet.ecocar.map

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.composeapp.BuildConfig
import com.fleet.ecocar.theme.EcoCarColors
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asBoolean
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.milliseconds

private const val MAPTILER_DARK_BASE = "https://api.maptiler.com/maps/streets-v2-dark/style.json"
private const val DEFAULT_LATITUDE = 52.52
private const val DEFAULT_LONGITUDE = 13.405
private const val DEFAULT_ZOOM = 11.0
private const val SINGLE_STATION_ZOOM = 14.0

private val PurpleUnknown = Color(0xFF9C27B0)
private val GreenAvailable = Color(0xFF4CAF50)

@Composable
actual fun EcoMapContent(
    modifier: Modifier,
    stations: List<EcoChargingStation>,
    onRefreshStations: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onRefreshStations()
    }

    LaunchedEffect(stations.size) {
        Log.d("ChargingStationMapScreen", "ChargingStationMapScreen: rendering ${stations.size} stations")
    }

    val styleUri = remember(BuildConfig.MAPTILER_KEY) {
        val key = BuildConfig.MAPTILER_KEY
        if (key.isBlank()) MAPTILER_DARK_BASE else "$MAPTILER_DARK_BASE?key=$key"
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(0.55f).fillMaxWidth()) {
            ChargingStationMap(
                modifier = Modifier.fillMaxSize(),
                styleUri = styleUri,
                stations = stations,
            )
            TextButton(
                onClick = onRefreshStations,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            ) {
                Text("Aktualisieren", color = EcoCarColors.GoldenYellow)
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
                text = "Ladestationen (${stations.size})",
                style = MaterialTheme.typography.titleMedium,
                color = EcoCarColors.OnDark,
            )
            Text(
                text = "Grün = verfügbar · Lila = Status unbekannt (Offline-Cache)",
                style = MaterialTheme.typography.labelSmall,
                color = EcoCarColors.OnDarkSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            if (stations.isEmpty()) {
                Text(
                    text = "Keine Stationen in Reichweite oder MQTT nicht erreichbar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EcoCarColors.OnDarkSecondary,
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(stations, key = { it.stationId }) { station ->
                        StationRow(station)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChargingStationMap(
    modifier: Modifier,
    styleUri: String,
    stations: List<EcoChargingStation>,
) {
    val cameraState = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(longitude = DEFAULT_LONGITUDE, latitude = DEFAULT_LATITUDE),
            zoom = DEFAULT_ZOOM,
        ),
    )

    val geoJsonData = remember(stations) {
        GeoJsonData.JsonString(buildStationsGeoJson(stations))
    }

    LaunchedEffect(stations) {
        when {
            stations.isEmpty() -> Unit
            stations.size == 1 -> {
                val station = stations.first()
                cameraState.animateTo(
                    finalPosition = CameraPosition(
                        target = Position(longitude = station.longitude, latitude = station.latitude),
                        zoom = SINGLE_STATION_ZOOM,
                    ),
                    duration = 400.milliseconds,
                )
            }
            else -> {
                cameraState.animateTo(
                    boundingBox = stationsBoundingBox(stations),
                    padding = PaddingValues(48.dp),
                    duration = 400.milliseconds,
                )
            }
        }
    }

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Uri(styleUri),
        cameraState = cameraState,
    ) {
        val stationSource = rememberGeoJsonSource(data = geoJsonData)
        CircleLayer(
            id = "charging-stations",
            source = stationSource,
            radius = const(8.dp),
            strokeColor = const(Color.White),
            strokeWidth = const(2.dp),
            color = switch(
                condition(
                    test = feature["available"].asBoolean(),
                    output = const(GreenAvailable),
                ),
                fallback = const(PurpleUnknown),
            ),
        )
    }
}

internal fun buildStationsGeoJson(stations: List<EcoChargingStation>): String {
    if (stations.isEmpty()) {
        return """{"type":"FeatureCollection","features":[]}"""
    }
    val features = stations.joinToString(separator = ",") { station ->
        val available = stationAvailable(station)
        """{"type":"Feature","geometry":{"type":"Point","coordinates":[${station.longitude},${station.latitude}]},"properties":{"available":$available}}"""
    }
    return """{"type":"FeatureCollection","features":[$features]}"""
}

private fun stationAvailable(station: EcoChargingStation): Boolean =
    !station.isPurpleUnknown && station.status.equals("AVAILABLE", ignoreCase = true)

private fun stationsBoundingBox(stations: List<EcoChargingStation>): BoundingBox {
    val minLat = stations.minOf { it.latitude }
    val maxLat = stations.maxOf { it.latitude }
    val minLon = stations.minOf { it.longitude }
    val maxLon = stations.maxOf { it.longitude }
    val latPad = (maxLat - minLat).coerceAtLeast(0.01)
    val lonPad = (maxLon - minLon).coerceAtLeast(0.01)
    return BoundingBox(
        west = minLon - lonPad / 2,
        south = minLat - latPad / 2,
        east = maxLon + lonPad / 2,
        north = maxLat + latPad / 2,
    )
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
