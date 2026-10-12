package com.fleet.ecocar.infrastructure.map

import android.content.Context
import android.util.Log
import com.fleet.ecocar.composeapp.BuildConfig
import com.fleet.ecocar.map.VehicleMarkerGeoJson
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

class VehicleLocationLayerController(
    context: Context,
    private val style: Style,
) {
    private val bitmap = VehicleLocationBitmapFactory.createBitmap(context)
    private var retainedBearing: Double? = null

    fun setLocation(latitude: Double?, longitude: Double?, headingDegrees: Double?) {
        registerIconIfNeeded()
        val bearing = VehicleMarkerGeoJson.resolveBearing(headingDegrees, retainedBearing)
        if (bearing != null) retainedBearing = bearing

        val geoJson = if (latitude != null && longitude != null) {
            VehicleMarkerGeoJson.toGeoJson(longitude, latitude, bearing)
        } else {
            """{"type":"FeatureCollection","features":[]}"""
        }

        val existing = style.getSourceAs<GeoJsonSource>(SOURCE_ID)
        if (existing != null) {
            existing.setGeoJson(geoJson)
            if (BuildConfig.DEBUG) {
                Log.d(
                    "BmsAcceptanceGps",
                    "marker_rendered lat=$latitude lon=$longitude bearing=$bearing hasBearing=${headingDegrees != null}",
                )
            }
            return
        }

        style.addSource(GeoJsonSource(SOURCE_ID, geoJson))
        style.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.iconImage(ICON_ID),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconAnchor("center"),
                PropertyFactory.iconRotate(Expression.get("bearing")),
                PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
            ),
        )
        if (BuildConfig.DEBUG) {
            Log.d(
                "BmsAcceptanceGps",
                "marker_rendered lat=$latitude lon=$longitude bearing=$bearing hasBearing=${headingDegrees != null}",
            )
        }
    }

    private fun registerIconIfNeeded() {
        if (style.getImage(ICON_ID) == null) {
            style.addImage(ICON_ID, bitmap)
        }
    }

    private companion object {
        const val SOURCE_ID = "vehicle-location-source"
        const val LAYER_ID = "vehicle-location-layer"
        const val ICON_ID = "vehicle-location-pin"
    }
}
