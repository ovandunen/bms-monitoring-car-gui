package com.fleet.ecocar.infrastructure.map

import android.content.Context
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource

/**
 * Adds or updates the charging-station GeoJSON source and symbol layer on a MapLibre [Style].
 * No Compose imports — MapLibre Style API only.
 */
class StationPinLayerController(
    context: Context,
    private val style: Style,
) {
    private val pinBitmap = ChargingPinBitmapFactory.createBitmap(context)

    fun addOrUpdateLayer(geoJson: String) {
        registerPinImageIfNeeded()
        val existing = style.getSourceAs<GeoJsonSource>(SOURCE_ID)
        if (existing != null) {
            existing.setGeoJson(geoJson)
            return
        }
        style.addSource(GeoJsonSource(SOURCE_ID, geoJson))
        style.addLayer(
            SymbolLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.iconImage(ICON_ID),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.textField(Expression.get("name")),
                PropertyFactory.textAnchor("top"),
                PropertyFactory.textOffset(arrayOf(0f, 1.2f)),
                PropertyFactory.textSize(12f),
                PropertyFactory.textColor("#FFFFFF"),
                PropertyFactory.textAllowOverlap(true),
            ),
        )
    }

    private fun registerPinImageIfNeeded() {
        if (style.getImage(ICON_ID) == null) {
            style.addImage(ICON_ID, pinBitmap)
        }
    }

    private companion object {
        const val SOURCE_ID = "charging-stations-source"
        const val LAYER_ID = "charging-stations-layer"
        const val ICON_ID = "charging-pin"
    }
}
