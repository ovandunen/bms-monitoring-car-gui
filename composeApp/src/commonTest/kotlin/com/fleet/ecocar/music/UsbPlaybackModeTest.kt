package com.fleet.ecocar.music

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

class UsbPlaybackModeTest {

    @Test
    fun playerModeConfig_wholeListRepeatsInOrder() {
        val config = playerModeConfig(UsbPlaybackMode.WHOLE_LIST)
        assertTrue(config.repeatAll)
        assertFalse(config.shuffleEnabled)
    }

    @Test
    fun playerModeConfig_shufflePlaysOnce() {
        val config = playerModeConfig(UsbPlaybackMode.SHUFFLE)
        assertFalse(config.repeatAll)
        assertFalse(config.shuffleEnabled)
    }

    @Test
    fun usbQueueOrder_wholeListKeepsSourceOrder() {
        assertEquals(listOf(0, 1, 2, 3), usbQueueOrder(4, 2, UsbPlaybackMode.WHOLE_LIST, Random(1)))
    }

    @Test
    fun usbQueueOrder_shuffleStartsWithCurrentAndContainsEachIndexOnce() {
        val current = 2
        val order = usbQueueOrder(5, current, UsbPlaybackMode.SHUFFLE, Random(7))
        assertEquals(current, order.first())
        assertEquals((0 until 5).toSet(), order.toSet())
        assertEquals(5, order.size)
    }

    @Test
    fun usbQueueOrder_shuffleOfSingleTrackIsThatTrack() {
        assertEquals(listOf(0), usbQueueOrder(1, 0, UsbPlaybackMode.SHUFFLE, Random(1)))
    }

    @Test
    fun fromStored_unknownValueUsesWholeList() {
        assertEquals(UsbPlaybackMode.WHOLE_LIST, UsbPlaybackMode.fromStored(null))
        assertEquals(UsbPlaybackMode.WHOLE_LIST, UsbPlaybackMode.fromStored("NOPE"))
        assertEquals(UsbPlaybackMode.SHUFFLE, UsbPlaybackMode.fromStored("SHUFFLE"))
    }

    @Test
    fun playbackModeStore_roundTrip() {
        val dir = java.io.File(
            System.getProperty("java.io.tmpdir"),
            "playback-mode-${System.nanoTime()}",
        )
        dir.mkdirs()
        val file = java.io.File(dir, "mode.preferences_pb")
        val store = PlaybackModeStore(
            PreferenceDataStoreFactory.createWithPath(produceFile = { file.absolutePath.toPath() }),
        )
        try {
            runBlocking {
                assertEquals(UsbPlaybackMode.WHOLE_LIST, store.load())
                store.save(UsbPlaybackMode.SHUFFLE)
                assertEquals(UsbPlaybackMode.SHUFFLE, store.load())
                store.save(UsbPlaybackMode.WHOLE_LIST)
                assertEquals(UsbPlaybackMode.WHOLE_LIST, store.load())
            }
        } finally {
            dir.deleteRecursively()
        }
    }
}
