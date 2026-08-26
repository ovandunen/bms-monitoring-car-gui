package com.fleet.ecocar.infrastructure.navigation

import android.content.Context
import android.widget.Toast
import com.fleet.ecocar.application.navigation.ResolveSwapDestinationUseCase
import com.fleet.ecocar.domain.navigation.GraphReadiness
import com.fleet.ecocar.domain.navigation.LatLon
import com.fleet.ecocar.domain.navigation.NavigationStorageBudget
import com.fleet.ecocar.map.EcoChargingStation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class VehicleNavigationCoordinator(
    private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val routingGraph = OnDeviceRoutingGraph(context)
    private val depotSync = GraphDepotSyncTrigger(context, routingGraph)
    private val resolveDestination = ResolveSwapDestinationUseCase()

    val graphReadiness: StateFlow<GraphReadiness> = routingGraph.readiness

    fun initialize() {
        scope.launch(Dispatchers.IO) {
            routingGraph.ensureLoaded()
            depotSync.evaluateAndMaybeDownload()
        }
    }

    fun navigateToAcceptedSwapStation(
        stationId: String,
        stations: List<EcoChargingStation>,
    ) {
        scope.launch {
            val destination = resolveDestination.resolve(stationId, stations)
            if (destination == null) {
                toast("Station location unavailable")
                return@launch
            }
            val available = withContext(Dispatchers.IO) {
                context.filesDir.freeSpace
            }
            if (!NavigationStorageBudget.hasSpaceForRouting(available)) {
                toast("Insufficient storage for navigation")
                return@launch
            }
            when (routingGraph.ensureLoaded()) {
                GraphReadiness.NotLoaded, GraphReadiness.Loading, GraphReadiness.Error -> {
                    toast("Map data not ready")
                    return@launch
                }
                GraphReadiness.Ready -> Unit
            }
            val origin = currentLocation() ?: run {
                toast("GPS fix unavailable")
                return@launch
            }
            val routeResult = routingGraph.route(origin, destination.coordinates)
            routeResult.onSuccess { json ->
                TurnByTurnNavigationLauncher.start(context, json)
            }.onFailure {
                toast("Could not compute route")
            }
        }
    }

    private suspend fun currentLocation(): LatLon? = withContext(Dispatchers.IO) {
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cancel = CancellationTokenSource()
            val loc = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancel.token).await()
            loc?.let { LatLon(it.latitude, it.longitude) }
        } catch (_: Exception) {
            null
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
