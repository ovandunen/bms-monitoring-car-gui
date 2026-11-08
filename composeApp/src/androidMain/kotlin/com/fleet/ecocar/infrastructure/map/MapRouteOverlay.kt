package com.fleet.ecocar.infrastructure.map

import com.fleet.ecocar.domain.navigation.GeoPoint

/** Route line plus the station-pin tap. One argument so the map host stays at eight parameters. */
class MapRouteOverlay(
    val points: List<GeoPoint>,
    val onStationTap: (String) -> Unit,
)
