package com.fleet.ecocar.infrastructure.map

import android.content.Context
import android.util.Log
import com.fleet.ecocar.composeapp.BuildConfig
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

class VehicleLocationLayerController(
    context: Context,
    private val style: Style,
) {
    private val bitmap = VehicleLocationBitmapFactory.createBitmap(context)

    fun setLocation(latitude: Double?, longitude: Double?) {
        registerIconIfNeeded()

        val geoJson = if (latitude != null && longitude != null) {
            """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[$longitude,$latitude]},"properties":{}}]}"""
        } else {
            """{"type":"FeatureCollection","features":[]}"""
        }

        val existing = style.getSourceAs<GeoJsonSource>(SOURCE_ID)
        if (existing != null) {
            existing.setGeoJson(geoJson)
            if (BuildConfig.DEBUG) {
                Log.d("BmsAcceptanceGps", "marker_rendered lat=$latitude lon=$longitude")
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
            ),
        )
        if (BuildConfig.DEBUG) {
            Log.d("BmsAcceptanceGps", "marker_rendered lat=$latitude lon=$longitude")
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
