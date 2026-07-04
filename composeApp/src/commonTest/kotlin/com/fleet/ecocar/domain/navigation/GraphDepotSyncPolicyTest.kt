package com.fleet.ecocar.domain.navigation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GraphDepotSyncPolicyTest {

    @Test
    fun wifiAndStaleGraph_triggersDownload() {
        assertTrue(
            GraphDepotSyncPolicy.shouldTriggerDownload(
                isWifi = true,
                isGraphStale = true,
                availableBytes = NavigationStorageBudget.GRAPH_UPDATE_REQUIRED_BYTES,
            ),
        )
    }

    @Test
    fun cellularAndStaleGraph_doesNotTriggerDownload() {
        assertFalse(
            GraphDepotSyncPolicy.shouldTriggerDownload(
                isWifi = false,
                isGraphStale = true,
                availableBytes = NavigationStorageBudget.GRAPH_UPDATE_REQUIRED_BYTES,
            ),
        )
    }
}
