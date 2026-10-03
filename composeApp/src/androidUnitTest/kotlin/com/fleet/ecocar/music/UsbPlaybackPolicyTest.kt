package com.fleet.ecocar.music

import android.net.Uri
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class UsbPlaybackPolicyTest {

    private val playing = Uri.parse("content://media/external/audio/media/7")
    private val other = Uri.parse("content://media/external/audio/media/8")

    @Test
    fun shouldScan_whenPermissionGrantedAndUsbTab_isTrue() {
        assertTrue(shouldScanUsb(permissionGranted = true, tabIsUsb = true))
    }

    @Test
    fun shouldScan_whenPermissionMissing_isFalse() {
        assertFalse(shouldScanUsb(permissionGranted = false, tabIsUsb = true))
        assertFalse(shouldScanUsb(permissionGranted = false, tabIsUsb = false))
    }

    @Test
    fun usbTrackUriIsPresent_whenUriInList_isTrue() {
        assertTrue(usbTrackUriIsPresent(playing, listOf(track(playing))))
    }

    @Test
    fun usbTrackUriIsPresent_whenUriAbsent_isFalse() {
        assertFalse(usbTrackUriIsPresent(playing, listOf(track(other))))
        assertFalse(usbTrackUriIsPresent(playing, emptyList()))
    }

    @Test
    fun shouldStopUsbAfterRescan_whenRadio_isFalse() {
        assertFalse(
            shouldStopUsbAfterRescan(
                surface = MusicPlaybackSurface.RADIO,
                playingUri = playing,
                tracks = emptyList(),
            ),
        )
    }

    @Test
    fun shouldStopUsbAfterRescan_whenUsbUriGone_isTrue() {
        assertTrue(
            shouldStopUsbAfterRescan(
                surface = MusicPlaybackSurface.USB,
                playingUri = playing,
                tracks = listOf(track(other)),
            ),
        )
    }

    @Test
    fun shouldStopUsbAfterRescan_whenUsbUriRemains_isFalse() {
        assertFalse(
            shouldStopUsbAfterRescan(
                surface = MusicPlaybackSurface.USB,
                playingUri = playing,
                tracks = listOf(track(playing)),
            ),
        )
    }

    private fun track(uri: Uri) = Track(
        id = 1L,
        title = "t",
        artist = "a",
        album = "al",
        uri = uri,
        albumArtUri = null,
        durationMs = 1L,
    )
}
