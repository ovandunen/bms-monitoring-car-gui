package com.fleet.ecocar.infrastructure.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.fleet.ecocar.domain.map.ChargingStation
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * MapLibre [MapView] host that applies [StationPinLayerController] when the style is ready
 * and whenever [stations] changes.
 */
@Composable
fun MapViewWithStationPins(
    styleUri: String,
    stations: List<ChargingStation>,
    vehicleLatitude: Double? = null, 
    vehicleLongitude: Double? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleReady by remember { mutableStateOf(false) }
    val geoJsonAdapter = remember { GeoJsonStationLayerAdapter() }

    MapViewLifecycleBinding(mapView)

    DisposableEffect(mapView, styleUri) {
        mapView.getMapAsync { map ->
            mapRef = map
            map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
                styleReady = true
                StationPinLayerController(context, style)
                    .addOrUpdateLayer(geoJsonAdapter.toGeoJson(stations))
                VehicleLocationLayerController(context, style)
                    .setLocation(vehicleLatitude, vehicleLongitude)
            }
        }
        onDispose {
            styleReady = false
            mapRef = null
        }
    }

    LaunchedEffect(stations, styleReady, mapRef) {
        if (!styleReady) return@LaunchedEffect
        val map = mapRef ?: return@LaunchedEffect
        map.getStyle { style ->
            StationPinLayerController(context, style)
                .addOrUpdateLayer(geoJsonAdapter.toGeoJson(stations))
        }
    }

    LaunchedEffect(vehicleLatitude, vehicleLongitude, styleReady, mapRef) {
        if (!styleReady) return@LaunchedEffect
        val map = mapRef ?: return@LaunchedEffect
        map.getStyle { style ->
            VehicleLocationLayerController(context, style)
                .setLocation(vehicleLatitude, vehicleLongitude)
        }
        // ← CAMERA TRACKING GOES HERE, inside the same LaunchedEffect
        if (vehicleLatitude != null && vehicleLongitude != null) {
            map.animateCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                    org.maplibre.android.geometry.LatLng(vehicleLatitude, vehicleLongitude),
                    15.0  // or keep current zoom: map.cameraPosition.zoom
                ),
                300   // duration ms
            )
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}
