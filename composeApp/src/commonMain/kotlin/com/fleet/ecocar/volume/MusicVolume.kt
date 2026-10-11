package com.fleet.ecocar.volume

import kotlin.math.roundToInt

data class MusicVolumeLevel(
    val index: Int,
    val maxIndex: Int,
)

fun sliderFraction(index: Int, maxIndex: Int): Float {
    if (maxIndex <= 0) return 0f
    return index.coerceIn(0, maxIndex).toFloat() / maxIndex.toFloat()
}

fun indexFromSlider(fraction: Float, maxIndex: Int): Int {
    if (maxIndex <= 0) return 0
    return (fraction.coerceIn(0f, 1f) * maxIndex).roundToInt().coerceIn(0, maxIndex)
}

fun steppedVolumeIndex(index: Int, delta: Int, maxIndex: Int): Int {
    if (maxIndex <= 0) return 0
    return (index + delta).coerceIn(0, maxIndex)
}

interface MusicVolumeControl {
    fun read(): MusicVolumeLevel
    fun setIndex(index: Int)
    fun observe(onChange: (MusicVolumeLevel) -> Unit): MusicVolumeSubscription
}

fun interface MusicVolumeSubscription {
    fun cancel()
}
