package com.fleet.ecocar.infrastructure.navigation

import android.content.Context
import com.fleet.ecocar.domain.navigation.GraphReadiness
import com.fleet.ecocar.domain.navigation.LatLon
import com.fleet.ecocar.domain.navigation.RouteProvider
import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Loads the on-device Senegal+Gambia graph and routes via embedded GraphHopper.
 */
class OnDeviceRoutingGraph(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RouteProvider {
    private val graphDir: File
        get() = File(context.filesDir, ROUTING_DIR).apply { mkdirs() }

    private val _readiness = MutableStateFlow(GraphReadiness.NotLoaded)
    val readiness: StateFlow<GraphReadiness> = _readiness.asStateFlow()

    @Volatile
    private var hopper: GraphHopper? = null

    suspend fun ensureLoaded(): GraphReadiness = withContext(ioDispatcher) {
        if (_readiness.value == GraphReadiness.Ready && hopper != null) {
            return@withContext GraphReadiness.Ready
        }
        if (_readiness.value == GraphReadiness.Loading) {
            return@withContext _readiness.value
        }
        _readiness.value = GraphReadiness.Loading
        try {
            val graphLocation = File(graphDir, GRAPH_FOLDER)
            if (!graphLocation.exists()) {
                _readiness.value = GraphReadiness.NotLoaded
                return@withContext GraphReadiness.NotLoaded
            }
            val engine = GraphHopper().also { hopper = it }
            engine.init(graphHopperCarConfig())
            engine.setGraphHopperLocation(graphLocation.absolutePath)
            if (!engine.load()) {
                _readiness.value = GraphReadiness.Error
                return@withContext GraphReadiness.Error
            }
            _readiness.value = GraphReadiness.Ready
            GraphReadiness.Ready
        } catch (_: Exception) {
            _readiness.value = GraphReadiness.Error
            GraphReadiness.Error
        }
    }

    override suspend fun route(origin: LatLon, destination: LatLon): Result<String> =
        withContext(ioDispatcher) {
            when (ensureLoaded()) {
                GraphReadiness.Ready -> Unit
                GraphReadiness.NotLoaded -> return@withContext Result.failure(IllegalStateException("map data not ready"))
                GraphReadiness.Loading -> return@withContext Result.failure(IllegalStateException("map data loading"))
                GraphReadiness.Error -> return@withContext Result.failure(IllegalStateException("map data error"))
            }
            val engine = hopper ?: return@withContext Result.failure(IllegalStateException("map data not ready"))
            val request = GHRequest(origin.latitude, origin.longitude, destination.latitude, destination.longitude)
                .setProfile("car")
                .setLocale("en")
            val response = engine.route(request)
            if (response.hasErrors()) {
                return@withContext Result.failure(IllegalStateException(response.errors.first().message))
            }
            Result.success(GhNavigateResponseConverter.toDirectionsResponseJson(response))
        }

    fun localGraphVersion(): String? =
        File(graphDir, VERSION_FILE).takeIf { it.exists() }?.readText()?.trim()?.ifBlank { null }

    fun graphStorageDir(): File = graphDir

    companion object {
        const val ROUTING_DIR = "routing"
        const val GRAPH_FOLDER = "senegal-and-gambia-gh"
        const val VERSION_FILE = "graph-version.txt"
        const val GRAPH_ARCHIVE_NAME = "senegal-and-gambia-gh.zip"
    }
}
