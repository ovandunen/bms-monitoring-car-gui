package com.fleet.ecocar.infrastructure.map

import com.fleet.ecocar.composeapp.BuildConfig
import com.fleet.ecocar.domain.map.port.MapStyleRepository

/**
 * Infrastructure adapter: sole owner of MapTiler URL format and [BuildConfig] key source.
 */
class MapTilerStyleAdapter : MapStyleRepository {

    override fun getStyleUrl(): String =
        "https://api.maptiler.com/maps/streets-v2-dark/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
}
