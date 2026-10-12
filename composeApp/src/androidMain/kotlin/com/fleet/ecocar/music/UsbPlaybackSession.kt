package com.fleet.ecocar.music

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import kotlin.random.Random

data class PlaybackSetup(
    val items: List<MediaItem>,
    val startIndex: Int,
    val startPositionMs: Long,
    val repeatAll: Boolean,
)

class UsbPlaybackSession(
    private val random: Random = Random.Default,
) {
    private var original: List<MediaItem> = emptyList()

    fun onNewQueue(items: List<MediaItem>, startIndex: Int, mode: UsbPlaybackMode): PlaybackSetup {
        original = items
        val safe = if (items.isEmpty()) 0 else startIndex.coerceIn(0, items.lastIndex)
        return arrange(safe, C.TIME_UNSET, mode)
    }

    fun onModeChange(currentMediaId: String?, positionMs: Long, mode: UsbPlaybackMode): PlaybackSetup? {
        if (original.isEmpty()) return null
        val found = original.indexOfFirst { it.mediaId == currentMediaId }
        val index = if (found < 0) 0 else found
        return arrange(index, positionMs, mode)
    }

    fun clear() {
        original = emptyList()
    }

    private fun arrange(indexInOriginal: Int, positionMs: Long, mode: UsbPlaybackMode): PlaybackSetup {
        val order = usbQueueOrder(original.size, indexInOriginal, mode, random)
        val start = if (mode == UsbPlaybackMode.SHUFFLE) 0 else indexInOriginal
        return PlaybackSetup(
            items = order.map { original[it] },
            startIndex = start,
            startPositionMs = positionMs,
            repeatAll = playerModeConfig(mode).repeatAll,
        )
    }
}
