package com.fleet.ecocar.infrastructure.map

import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.fleet.ecocar.domain.map.ChargingStation
import com.fleet.ecocar.map.steppedMapZoom
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
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
    vehicleHeadingDegrees: Double? = null,
    zoomPort: MapZoomPort,
    modifier: Modifier = Modifier,
    route: MapRouteOverlay = MapRouteOverlay(emptyList()) { _: String -> },
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleReady by remember { mutableStateOf(false) }
    var locationController by remember { mutableStateOf<VehicleLocationLayerController?>(null) }
    val geoJsonAdapter = remember { GeoJsonStationLayerAdapter() }
    var followedVehicle by remember { mutableStateOf(false) }
    val tapState = rememberUpdatedState(route.onStationTap)
    val tapInstalled = remember(mapView) { mutableStateOf(false) }

    MapViewLifecycleBinding(mapView)

    DisposableEffect(mapView, styleUri) {
        mapView.getMapAsync { map ->
            mapRef = map
            zoomPort.bind { delta ->
                val next = steppedMapZoom(
                    current = map.cameraPosition.zoom,
                    delta = delta,
                    minZoom = map.minZoomLevel,
                    maxZoom = map.maxZoomLevel,
                )
                map.animateCamera(CameraUpdateFactory.zoomTo(next), CAMERA_MOVE_MS)
            }
            if (!tapInstalled.value) {
                tapInstalled.value = true
                map.addOnMapClickListener { latLng ->
                    dispatchStationTap(mapView, map, latLng, tapState.value)
                }
            }
            map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
                styleReady = true
                StationPinLayerController(context, style)
                    .addOrUpdateLayer(geoJsonAdapter.toGeoJson(stations))
                locationController = VehicleLocationLayerController(context, style).also {
                    it.setLocation(vehicleLatitude, vehicleLongitude, vehicleHeadingDegrees)
                }
            }
        }
        onDispose {
            zoomPort.unbind()
            styleReady = false
            mapRef = null
            locationController = null
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

    LaunchedEffect(vehicleLatitude, vehicleLongitude, vehicleHeadingDegrees, locationController) {
        locationController?.setLocation(vehicleLatitude, vehicleLongitude, vehicleHeadingDegrees)
    }

    LaunchedEffect(vehicleLatitude, vehicleLongitude, styleReady, mapRef) {
        if (!styleReady) return@LaunchedEffect
        val map = mapRef ?: return@LaunchedEffect
        if (vehicleLatitude != null && vehicleLongitude != null) {
            val target = LatLng(vehicleLatitude, vehicleLongitude)
            val update = if (followedVehicle) {
                CameraUpdateFactory.newLatLng(target)
            } else {
                CameraUpdateFactory.newLatLngZoom(target, INITIAL_ZOOM)
            }
            map.animateCamera(update, CAMERA_MOVE_MS)
            followedVehicle = true
        }
    }

    LaunchedEffect(route.points, styleReady, mapRef) {
        if (!styleReady) return@LaunchedEffect
        val map = mapRef ?: return@LaunchedEffect
        map.getStyle { style ->
            try {
                RouteLineLayerController(style).setRoute(RouteLineGeoJson.toGeoJson(route.points))
            } catch (_: Exception) {
                // A bad line must not take the live map down.
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

/** 32 dp is half of [com.fleet.ecocar.ui.DriverTouch.MinTarget], as a finger box around the pin. */
private fun dispatchStationTap(
    mapView: MapView,
    map: MapLibreMap,
    latLng: LatLng,
    onStationTap: (String) -> Unit,
): Boolean {
    return try {
        val point = map.projection.toScreenLocation(latLng)
        val slop = mapView.resources.displayMetrics.density * 32f
        val box = RectF(point.x - slop, point.y - slop, point.x + slop, point.y + slop)
        val id = map.queryRenderedFeatures(box, StationPinLayerController.LAYER_ID)
            .firstOrNull()
            ?.getStringProperty("id")
        if (id.isNullOrEmpty()) {
            false
        } else {
            onStationTap(id)
            true
        }
    } catch (_: Exception) {
        false
    }
}

private const val INITIAL_ZOOM = 15.0
private const val CAMERA_MOVE_MS = 300
