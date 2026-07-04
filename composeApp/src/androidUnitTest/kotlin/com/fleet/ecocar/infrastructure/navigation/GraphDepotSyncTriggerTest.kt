package com.fleet.ecocar.infrastructure.navigation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class GraphDepotSyncTriggerTest {

    @Test
    fun wifiAndStaleGraph_setsDownloadStarted() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val graph = OnDeviceRoutingGraph(context)
        val trigger = GraphDepotSyncTrigger(
            context = context,
            routingGraph = graph,
            isWifiConnected = { true },
            fetchRemoteVersion = { "2026-07-03" },
            availableBytes = { 600_000_000L },
        )
        graph.graphStorageDir().mkdirs()
        graph.graphStorageDir().resolve(OnDeviceRoutingGraph.VERSION_FILE).writeText("2026-07-01")

        val started = kotlinx.coroutines.runBlocking {
            trigger.evaluateAndMaybeDownload(remoteVersion = "2026-07-03")
        }

        assertTrue(started)
        assertTrue(trigger.downloadStarted)
    }

    @Test
    fun cellularAndStaleGraph_doesNotStartDownload() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val graph = OnDeviceRoutingGraph(context)
        val trigger = GraphDepotSyncTrigger(
            context = context,
            routingGraph = graph,
            isWifiConnected = { false },
            fetchRemoteVersion = { "2026-07-03" },
            availableBytes = { 600_000_000L },
        )

        val started = kotlinx.coroutines.runBlocking {
            trigger.evaluateAndMaybeDownload(remoteVersion = "2026-07-03")
        }

        assertFalse(started)
        assertFalse(trigger.downloadStarted)
    }
}
