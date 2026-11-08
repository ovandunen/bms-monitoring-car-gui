package com.fleet.ecocar.infrastructure.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.fleet.ecocar.theme.EcoCarColors
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

/** One line layer. A null GeoJSON removes the source and the layer. */
class RouteLineLayerController(
    private val style: Style,
) {
    fun setRoute(geoJson: String?) {
        if (geoJson == null) {
            if (style.getLayer(LAYER_ID) != null) style.removeLayer(LAYER_ID)
            if (style.getSource(SOURCE_ID) != null) style.removeSource(SOURCE_ID)
            return
        }
        val existing = style.getSourceAs<GeoJsonSource>(SOURCE_ID)
        if (existing != null) {
            existing.setGeoJson(geoJson)
            return
        }
        style.addSource(GeoJsonSource(SOURCE_ID, geoJson))
        val line = LineLayer(LAYER_ID, SOURCE_ID).withProperties(
            PropertyFactory.lineColor(accentHex(EcoCarColors.GoldenYellow)),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        )
        if (style.getLayer(StationPinLayerController.LAYER_ID) != null) {
            style.addLayerBelow(line, StationPinLayerController.LAYER_ID)
        } else {
            style.addLayer(line)
        }
    }

    private fun accentHex(color: Color): String =
        String.format("#%06X", color.toArgb() and 0x00FFFFFF)

    private companion object {
        const val SOURCE_ID = "route-line-source"
        const val LAYER_ID = "route-line-layer"
    }
}
