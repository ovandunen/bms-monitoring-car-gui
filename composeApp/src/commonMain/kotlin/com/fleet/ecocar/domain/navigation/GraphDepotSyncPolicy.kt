package com.fleet.ecocar.domain.navigation

/**
 * Wi-Fi-only depot sync: never download routing graph over cellular.
 */
object GraphDepotSyncPolicy {
    fun shouldTriggerDownload(
        isWifi: Boolean,
        isGraphStale: Boolean,
        availableBytes: Long,
    ): Boolean =
        isWifi && isGraphStale && NavigationStorageBudget.hasSpaceForGraphUpdate(availableBytes)
}
