package com.fleet.ecocar.volume

import kotlin.math.roundToInt

data class MusicVolumeLevel(
    val index: Int,
    val maxIndex: Int,
    val muted: Boolean = false,
)

/**
 * STREAM_MUSIC level only. Mute is index 0 with the previous audible index kept,
 * so playback position is untouched.
 */
data class MediaVolume(
    val index: Int,
    val maxIndex: Int,
    val muted: Boolean,
    val restoreIndex: Int,
)

fun MediaVolume.toLevel(): MusicVolumeLevel = MusicVolumeLevel(index, maxIndex, muted)

fun MediaVolume.toggleMute(): MediaVolume {
    if (maxIndex <= 0) return copy(index = 0, muted = true, restoreIndex = 1)
    val audible = !muted && index > 0
    return if (audible) {
        copy(index = 0, muted = true, restoreIndex = index)
    } else {
        val restore = restoreIndex.coerceIn(1, maxIndex)
        copy(index = restore, muted = false, restoreIndex = restore)
    }
}

fun MediaVolume.withChosenIndex(newIndex: Int): MediaVolume {
    if (maxIndex <= 0) return copy(index = 0, muted = true)
    val next = newIndex.coerceIn(0, maxIndex)
    return if (next == 0) {
        val restore = if (index > 0) index else restoreIndex.coerceIn(1, maxIndex)
        copy(index = 0, muted = true, restoreIndex = restore)
    } else {
        copy(index = next, muted = false, restoreIndex = next)
    }
}

fun MediaVolume.withObservedIndex(observed: Int, observedMax: Int): MediaVolume {
    val max = observedMax.coerceAtLeast(0)
    if (max == 0) return copy(index = 0, maxIndex = 0, muted = true, restoreIndex = 1)
    val next = observed.coerceIn(0, max)
    return if (next == 0) {
        copy(index = 0, maxIndex = max, muted = true, restoreIndex = restoreIndex.coerceIn(1, max))
    } else {
        copy(index = next, maxIndex = max, muted = false, restoreIndex = next)
    }
}

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
    fun toggleMute()
    fun observe(onChange: (MusicVolumeLevel) -> Unit): MusicVolumeSubscription
}

fun interface MusicVolumeSubscription {
    fun cancel()
}
