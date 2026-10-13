package com.fleet.ecocar.map

/** One map zoom step. MapLibre's own zoom-in is also 1.0. */
const val MAP_ZOOM_STEP = 1.0

/**
 * Next zoom after one tap. Adds [delta] (use [MAP_ZOOM_STEP] or its negative)
 * and keeps the result inside the map's min and max.
 */
fun steppedMapZoom(current: Double, delta: Double, minZoom: Double, maxZoom: Double): Double {
    val lower = minOf(minZoom, maxZoom)
    val upper = maxOf(minZoom, maxZoom)
    return (current + delta).coerceIn(lower, upper)
}
