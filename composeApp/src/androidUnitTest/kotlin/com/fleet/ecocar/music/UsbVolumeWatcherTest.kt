package com.fleet.ecocar.music

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class UsbVolumeWatcherTest {

    private val dispatcher = StandardTestDispatcher()
    private var watcher: UsbVolumeWatcher? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        watcher?.stop()
        Dispatchers.resetMain()
    }

    @Test
    fun mountBroadcast_emitsOneSignalAfterDebounce() = runTest(dispatcher) {
        val started = startWatcher()
        sendMount()
        runCurrent()
        advanceTimeBy(UsbVolumeWatcher.DEBOUNCE_MS - 1)
        runCurrent()
        assertEquals(0L, started.changes.value)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(1L, started.changes.value)
    }

    @Test
    fun threeEventsWithinDebounce_emitOneSignal() = runTest(dispatcher) {
        val started = startWatcher()
        repeat(3) {
            sendMount()
            runCurrent()
            advanceTimeBy(500)
            runCurrent()
        }
        advanceTimeBy(UsbVolumeWatcher.DEBOUNCE_MS)
        runCurrent()
        assertEquals(1L, started.changes.value)
    }

    @Test
    fun mediaStoreChange_emitsSignalAfterDebounce() = runTest(dispatcher) {
        val started = startWatcher()
        val context = RuntimeEnvironment.getApplication() as Context
        context.contentResolver.notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null)
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        runCurrent()
        advanceTimeBy(UsbVolumeWatcher.DEBOUNCE_MS)
        runCurrent()
        assertEquals(1L, started.changes.value)
    }

    private fun TestScope.startWatcher(): UsbVolumeWatcher {
        val context = RuntimeEnvironment.getApplication() as Context
        // backgroundScope is cancelled when the test ends. The collector never completes on its own.
        val created = UsbVolumeWatcher(context, backgroundScope, debounceMs = UsbVolumeWatcher.DEBOUNCE_MS)
        watcher = created
        created.start()
        runCurrent()
        return created
    }

    private fun sendMount() {
        val context = RuntimeEnvironment.getApplication() as Context
        context.sendBroadcast(
            Intent(Intent.ACTION_MEDIA_MOUNTED).apply {
                data = Uri.parse("file:///storage/usb")
            },
        )
        // Robolectric queues the receiver on the main looper. The test scheduler does not drain it.
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }
}
