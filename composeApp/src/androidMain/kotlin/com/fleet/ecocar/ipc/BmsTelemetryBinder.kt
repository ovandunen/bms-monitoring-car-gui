package com.fleet.ecocar.ipc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.bms.monitor.aidl.BmsData
import com.bms.monitor.aidl.ChargingStationSnapshot
import com.bms.monitor.aidl.IBmsCallback
import com.bms.monitor.aidl.IBmsService
import com.fleet.ecocar.map.ChargingStationSnapshotMapper
import com.fleet.ecocar.map.EcoChargingStation
import com.fleet.ecocar.telemetry.EcoBmsTelemetry

/**
 * Binds EcoCar to BMS [IBmsService] for charging-station **data** (AIDL callbacks).
 * Map layout and when to query stations are owned by EcoCar — not BMS.
 */
class BmsTelemetryBinder(
    private val context: Context,
    private val onTelemetry: (EcoBmsTelemetry) -> Unit,
    private val onChargingStations: (List<EcoChargingStation>) -> Unit,
    private val onAlert: ((Int, String) -> Unit)? = null,
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var binder: IBmsService? = null

    @Volatile
    private var bound = false

    @Volatile
    private var bindRequested = false

    @Volatile
    private var pendingRefresh: RefreshRequest? = null

    private var bindRetryAttempt = 0
    private var bindRetryRunnable: Runnable? = null

    private data class RefreshRequest(val latitude: Double, val longitude: Double, val radiusMeters: Double)

    private val callback = object : IBmsCallback.Stub() {
        override fun onDataUpdate(data: BmsData) {
            val snap = EcoBmsTelemetry(
                timestamp = data.timestamp,
                cellVolts = data.cellVoltages.toList(),
                packTemperature = data.packTemperature,
                packHumidity = data.packHumidity,
                pm25 = data.pm25,
                pm10 = data.pm10,
                soc = data.soc,
                currentA = data.current,
            )
            mainHandler.post { onTelemetry(snap) }
        }

        override fun onAlert(level: Int, message: String?) {
            val msg = message.orEmpty()
            mainHandler.post { onAlert?.invoke(level, msg) }
        }

        override fun onChargingStationsUpdate(stations: Array<out ChargingStationSnapshot>?) {
            val mapped = stations?.map { it.toEcoChargingStation() }.orEmpty()
            mainHandler.post { onChargingStations(mapped) }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val svc = IBmsService.Stub.asInterface(service) ?: run {
                Log.w(TAG, "onServiceConnected but asInterface null ($name)")
                return
            }
            binder = svc
            bound = true
            bindRequested = true
            bindRetryAttempt = 0
            cancelBindRetry()
            Log.i(TAG, "onServiceConnected: $name — charging-station IPC ready")
            try {
                svc.registerCallback(callback)
                val cachedCount = publishCachedChargingStationsInternal(svc)
                Log.d(TAG, "onServiceConnected: published $cachedCount cached station(s) to GUI")
                flushPendingRefresh()
            } catch (e: Exception) {
                Log.e(TAG, "registerCallback failed", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "onServiceDisconnected: $name — will retry bind")
            binder = null
            bound = false
            bindRequested = false
            scheduleBindRetry()
        }
    }

    fun connect() {
        ensureBound()
    }

    /** (Re)bind to BmsService; safe to call from map refresh when the first bind has not completed yet. */
    private fun ensureBound() {
        if (bound && binder != null) return
        if (bindRequested) return

        Log.i(TAG, "ensureBound(): binding to $BMS_PACKAGE / $BMS_SERVICE_ACTION")
        val intent = serviceIntent()
        bindRequested = true
        try {
            appContext.startService(intent)
            val ok = appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            if (!ok) {
                bindRequested = false
                Log.w(
                    TAG,
                    "bindService returned false — is BMS APK installed? same debug signature? " +
                        "permission $BIND_BMS_PERMISSION required",
                )
                scheduleBindRetry()
            }
        } catch (e: SecurityException) {
            bindRequested = false
            Log.e(TAG, "bindService permission denied — gleiche Signatur wie BMS?", e)
            scheduleBindRetry()
        }
    }

    private fun scheduleBindRetry() {
        if (bound) return
        if (bindRetryAttempt >= MAX_BIND_RETRIES) {
            Log.e(TAG, "BmsService bind gave up after $MAX_BIND_RETRIES retries")
            return
        }
        cancelBindRetry()
        val delayMs = BIND_RETRY_DELAYS_MS.getOrElse(bindRetryAttempt) { 5_000L }
        bindRetryAttempt++
        bindRetryRunnable = Runnable {
            Log.w(TAG, "ensureBound retry #$bindRetryAttempt (BmsService still not bound)")
            bindRequested = false
            ensureBound()
            if (!bound) scheduleBindRetry()
        }
        mainHandler.postDelayed(bindRetryRunnable!!, delayMs)
    }

    private fun cancelBindRetry() {
        bindRetryRunnable?.let { mainHandler.removeCallbacks(it) }
        bindRetryRunnable = null
    }

    fun disconnect() {
        cancelBindRetry()
        try {
            binder?.unregisterCallback(callback)
        } catch (_: Exception) {
        }
        binder = null
        bound = false
        bindRequested = false
        bindRetryAttempt = 0
        pendingRefresh = null
        try {
            appContext.unbindService(connection)
        } catch (_: Exception) {
        }
    }

    /**
     * Publishes BMS in-memory / Room cache immediately (CSMS is not always available).
     * Safe to call on the main thread before a live refresh.
     */
    fun publishCachedChargingStations(): Boolean {
        ensureBound()
        val svc = binder ?: return false
        return publishCachedChargingStationsInternal(svc) > 0
    }

    /** EcoCar requests station pins for map display (IBmsService data API — not a map refresh in BMS). */
    fun requestChargingStationsForDisplay(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 0.0,
    ) {
        ensureBound()
        val svc = binder
        if (svc == null) {
            pendingRefresh = RefreshRequest(latitude, longitude, radiusMeters)
            scheduleBindRetry()
            Log.w(
                TAG,
                "requestChargingStationsForDisplay queued until BmsService bind " +
                    "(lat=$latitude lon=$longitude) — bind in progress, will flush when connected",
            )
            return
        }
        Log.d(TAG, "requestChargingStationsForDisplay lat=$latitude lon=$longitude radius=$radiusMeters")
        invokeRefresh(svc, latitude, longitude, radiusMeters)
    }

    private fun publishCachedChargingStationsInternal(svc: IBmsService): Int {
        return try {
            val cached = svc.getCachedChargingStations()
            val mapped = cached.map { it.toEcoChargingStation() }
            if (mapped.isNotEmpty()) {
                mainHandler.post { onChargingStations(mapped) }
                Log.d(TAG, "publishCachedChargingStations: ${mapped.size} station(s) from BMS cache")
            } else {
                Log.d(TAG, "publishCachedChargingStations: BMS cache empty")
            }
            mapped.size
        } catch (e: Exception) {
            Log.e(TAG, "publishCachedChargingStations failed", e)
            0
        }
    }

    private fun flushPendingRefresh() {
        val pending = pendingRefresh ?: return
        pendingRefresh = null
        Log.d(
            TAG,
            "flushPendingRefresh: lat=${pending.latitude} lon=${pending.longitude} " +
                "radius=${pending.radiusMeters}",
        )
        binder?.let { invokeRefresh(it, pending.latitude, pending.longitude, pending.radiusMeters) }
    }

    private fun invokeRefresh(
        svc: IBmsService,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
    ) {
        try {
            Log.d(TAG, "invokeRefresh → BmsService.refreshChargingStations lat=$latitude lon=$longitude")
            svc.refreshChargingStations(null, latitude, longitude, radiusMeters)
        } catch (e: Exception) {
            Log.e(TAG, "requestChargingStationsForDisplay failed", e)
        }
    }

    private fun serviceIntent(): Intent =
        Intent(BMS_SERVICE_ACTION).setPackage(BMS_PACKAGE)

    companion object {
        private const val TAG = "BmsTelemetryBinder"
        private const val BIND_BMS_PERMISSION = "com.ecocar.bms.BIND_BMS_SERVICE"
        const val BMS_PACKAGE = "com.fleet.bms"
        private const val BMS_SERVICE_ACTION = "com.ecocar.bms.action.BMS_SERVICE"
        private const val MAX_BIND_RETRIES = 5
        private val BIND_RETRY_DELAYS_MS = longArrayOf(500L, 1_000L, 2_000L, 3_000L, 5_000L)
    }
}

private fun ChargingStationSnapshot.toEcoChargingStation() =
    ChargingStationSnapshotMapper.toEco(
        ChargingStationSnapshotMapper.Fields(
            stationId = stationId,
            displayName = displayName,
            streetAddress = streetAddress,
            city = city,
            latitude = latitude,
            longitude = longitude,
            solarCapacityKw = solarCapacityKw,
            status = status,
            offlineCache = offlineCache,
        ),
    )
