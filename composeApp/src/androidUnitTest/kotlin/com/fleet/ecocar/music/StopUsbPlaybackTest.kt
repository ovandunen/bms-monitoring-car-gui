package com.fleet.ecocar.music

import com.fleet.ecocar.EcoCarApplication
import com.fleet.ecocar.ui.top.TopBarMusicState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28], application = MusicControlTestApplication::class)
class StopUsbPlaybackTest {

    @Test
    fun stopUsbPlayback_clearsSurfaceAndNowPlaying() {
        val app = RuntimeEnvironment.getApplication() as MusicControlTestApplication
        setSurface(app, MusicPlaybackSurface.USB)
        topBar(app).value = topBar(app).value.copy(
            title = "Song",
            source = "USB 1",
            isPlaying = true,
            currentPosition = 5L,
            clock = "12:00",
        )

        app.stopUsbPlayback()

        assertNull(app.musicPlaybackSurface)
        val state = app.topBarMusicState.value
        assertEquals("—", state.title)
        assertEquals("", state.source)
        assertEquals("0:00 / --:--", state.duration)
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPosition)
        assertEquals("12:00", state.clock)
    }

    @Test
    fun stopUsbPlayback_whenRadio_doesNothing() {
        val app = RuntimeEnvironment.getApplication() as MusicControlTestApplication
        setSurface(app, MusicPlaybackSurface.RADIO)
        topBar(app).value = topBar(app).value.copy(title = "Station", source = "Radio", clock = "12:00")

        app.stopUsbPlayback()

        assertEquals(MusicPlaybackSurface.RADIO, app.musicPlaybackSurface)
        assertEquals("Station", app.topBarMusicState.value.title)
        assertEquals("Radio", app.topBarMusicState.value.source)
    }

    private fun setSurface(app: EcoCarApplication, surface: MusicPlaybackSurface) {
        val field = EcoCarApplication::class.java.getDeclaredField("musicPlaybackSurface")
        field.isAccessible = true
        field.set(app, surface)
    }

    @Suppress("UNCHECKED_CAST")
    private fun topBar(app: EcoCarApplication): MutableStateFlow<TopBarMusicState> {
        val field = EcoCarApplication::class.java.getDeclaredField("_topBarMusic")
        field.isAccessible = true
        return field.get(app) as MutableStateFlow<TopBarMusicState>
    }
}

/** Skips MapLibre and the rest of [EcoCarApplication.onCreate]. */
class MusicControlTestApplication : EcoCarApplication() {
    override fun onCreate() = Unit
}
