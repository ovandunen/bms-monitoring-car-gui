package com.fleet.ecocar.domain.navigation

/**
 * Storage budget for offline routing on 32 GB fleet tablets (~1.2 GB reserved).
 */
object NavigationStorageBudget {
    /** Total routing footprint: graph + tile cache headroom + download staging. */
    const val TOTAL_BUDGET_BYTES: Long = 1_200_000_000L

    /** Expected Senegal+Gambia graph archive + unpack staging. */
    const val GRAPH_UPDATE_REQUIRED_BYTES: Long = 500_000_000L

    fun hasSpaceForGraphUpdate(availableBytes: Long): Boolean =
        availableBytes >= GRAPH_UPDATE_REQUIRED_BYTES

    fun hasSpaceForRouting(availableBytes: Long): Boolean =
        availableBytes >= GRAPH_UPDATE_REQUIRED_BYTES / 2
}
