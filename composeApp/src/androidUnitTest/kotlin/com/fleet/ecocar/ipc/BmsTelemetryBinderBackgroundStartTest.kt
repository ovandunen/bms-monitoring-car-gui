package com.fleet.ecocar.ipc

import android.app.BackgroundServiceStartNotAllowedException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.Looper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [31])
class BmsTelemetryBinderBackgroundStartTest {

    @Test
    fun startServiceThrowsBackgroundNotAllowed_doesNotEscape_bindRetryContinues() {
        val base = RuntimeEnvironment.getApplication()
        val context = ThrowingStartServiceContext(base).apply { bindSucceeds = false }
        val binder = BmsTelemetryBinder(
            context = context,
            onTelemetry = {},
            onChargingStations = {},
            onLocationUpdate = {},
        )

        binder.connect()
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(600))

        assertTrue(context.bindCalls >= 2)
    }

    @Test
    fun startServiceThrowsBackgroundNotAllowed_doesNotEscape_bindStillRuns() {
        val base = RuntimeEnvironment.getApplication()
        val context = ThrowingStartServiceContext(base)
        val binder = BmsTelemetryBinder(
            context = context,
            onTelemetry = {},
            onChargingStations = {},
            onLocationUpdate = {},
        )

        binder.connect()
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, context.bindCalls)
        assertTrue(context.bindRequestedFlag)
    }

    private class ThrowingStartServiceContext(base: Context) : ContextWrapper(base) {
        var bindCalls = 0
        var bindRequestedFlag = false
        var bindSucceeds = true

        override fun getApplicationContext(): Context = this

        override fun startService(service: Intent?): android.content.ComponentName? {
            throw BackgroundServiceStartNotAllowedException(
                "Not allowed to start service Intent { cmp=ch.ecocarsolaire.bms/ch.ecocar.bms.BmsMonitorService }: app is in background",
            )
        }

        override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
            bindCalls++
            bindRequestedFlag = true
            return bindSucceeds
        }
    }
}
