package com.fleet.ecocar.infrastructure.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fleet.ecocar.map.MAP_ZOOM_STEP

/** Bridge from the map overlay buttons to the MapLibre camera. */
class MapZoomPort {
    var ready by mutableStateOf(false)
        private set

    private var apply: ((Double) -> Unit)? = null

    fun bind(apply: (Double) -> Unit) {
        this.apply = apply
        ready = true
    }

    fun unbind() {
        apply = null
        ready = false
    }

    fun zoomIn() {
        apply?.invoke(MAP_ZOOM_STEP)
    }

    fun zoomOut() {
        apply?.invoke(-MAP_ZOOM_STEP)
    }
}
