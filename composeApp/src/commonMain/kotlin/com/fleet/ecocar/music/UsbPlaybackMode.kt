package com.fleet.ecocar.music

import kotlin.random.Random

enum class UsbPlaybackMode {
    WHOLE_LIST,
    SHUFFLE,
    ;

    companion object {
        fun fromStored(raw: String?): UsbPlaybackMode =
            entries.firstOrNull { it.name == raw } ?: WHOLE_LIST
    }
}

data class PlayerModeConfig(
    val repeatAll: Boolean,
    val shuffleEnabled: Boolean,
)

fun playerModeConfig(mode: UsbPlaybackMode): PlayerModeConfig = when (mode) {
    UsbPlaybackMode.WHOLE_LIST -> PlayerModeConfig(repeatAll = true, shuffleEnabled = false)
    UsbPlaybackMode.SHUFFLE -> PlayerModeConfig(repeatAll = false, shuffleEnabled = false)
}

/**
 * WHOLE_LIST keeps source order. SHUFFLE puts [currentIndex] first and
 * shuffles the remaining indexes, so every track is still played once.
 */
fun usbQueueOrder(
    count: Int,
    currentIndex: Int,
    mode: UsbPlaybackMode,
    random: Random,
): List<Int> {
    if (count <= 0) return emptyList()
    val current = currentIndex.coerceIn(0, count - 1)
    if (mode == UsbPlaybackMode.WHOLE_LIST) return (0 until count).toList()
    val rest = (0 until count).filter { it != current }.toMutableList()
    rest.shuffle(random)
    return listOf(current) + rest
}
