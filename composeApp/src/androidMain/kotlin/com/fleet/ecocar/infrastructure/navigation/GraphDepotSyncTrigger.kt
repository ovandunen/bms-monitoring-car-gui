package com.fleet.ecocar.infrastructure.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.StatFs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * On-device depot sync: Wi-Fi + stale graph + sufficient free space → background download.
 * Server-side graph publishing is out of scope; [manifestUrl] points at CSMS static hosting.
 */
class GraphDepotSyncTrigger(
    private val context: Context,
    private val routingGraph: OnDeviceRoutingGraph,
    private val manifestUrl: String = DEFAULT_MANIFEST_URL,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val isWifiConnected: () -> Boolean = { defaultIsWifiConnected(context) },
    private val fetchRemoteVersion: () -> String? = { fetchRemoteVersionFromUrl(manifestUrl) },
    private val availableBytes: () -> Long = { defaultAvailableBytes(routingGraph.graphStorageDir()) },
) {
    @Volatile
    var downloadStarted: Boolean = false
        private set

    suspend fun evaluateAndMaybeDownload(remoteVersion: String? = fetchRemoteVersion()): Boolean =
        withContext(ioDispatcher) {
            val remote = remoteVersion ?: return@withContext false
            val local = routingGraph.localGraphVersion()
            val stale = local == null || local != remote
            val should = com.fleet.ecocar.domain.navigation.GraphDepotSyncPolicy.shouldTriggerDownload(
                isWifi = isWifiConnected(),
                isGraphStale = stale,
                availableBytes = availableBytes(),
            )
            if (should) {
                downloadStarted = true
            }
            should
        }

    companion object {
        /** CSMS-hosted graph version manifest (see bms-central-management-system/tools/routing-graph). */
        const val DEFAULT_MANIFEST_URL =
            "https://csms.example.invalid/routing/senegal-and-gambia-graph-version.txt"

        private fun defaultIsWifiConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }

        private fun defaultAvailableBytes(dir: File): Long {
            dir.mkdirs()
            return StatFs(dir.absolutePath).availableBytes
        }

        private fun fetchRemoteVersionFromUrl(manifestUrl: String): String? =
            try {
                val connection = URL(manifestUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 5_000
                connection.readTimeout = 5_000
                connection.inputStream.bufferedReader().use { it.readText().trim().ifBlank { null } }
            } catch (_: Exception) {
                null
            }
    }
}
