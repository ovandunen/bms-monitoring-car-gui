package com.fleet.ecocar.domain.map.port

/**
 * Port: resolves the MapLibre base style URL (implementation supplies API credentials).
 */
interface MapStyleRepository {
    fun getStyleUrl(): String
}
