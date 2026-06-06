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
    private var pendingRefresh: RefreshRequest? = null

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
                Log.w(TAG, "asInterface null")
                return
            }
            binder = svc
            try {
                svc.registerCallback(callback)
                publishCachedChargingStationsInternal(svc)
                flushPendingRefresh()
            } catch (e: Exception) {
                Log.e(TAG, "registerCallback failed", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }
    }

    fun connect() {
        val intent = serviceIntent()
        try {
            appContext.startService(intent)
            val ok = appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            if (!ok) {
                Log.w(TAG, "bindService returned false (BMS APK installiert / Signatur BIND_BMS?)")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "bindService permission denied — gleiche Signatur wie BMS?", e)
        }
    }

    fun disconnect() {
        try {
            binder?.unregisterCallback(callback)
        } catch (_: Exception) {
        }
        binder = null
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
        val svc = binder ?: return false
        return publishCachedChargingStationsInternal(svc)
    }

    /** EcoCar requests station pins for map display (IBmsService data API — not a map refresh in BMS). */
    fun requestChargingStationsForDisplay(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 0.0,
    ) {
        val svc = binder
        if (svc == null) {
            pendingRefresh = RefreshRequest(latitude, longitude, radiusMeters)
            Log.d(TAG, "requestChargingStationsForDisplay queued until BmsService bind")
            return
        }
        invokeRefresh(svc, latitude, longitude, radiusMeters)
    }

    private fun publishCachedChargingStationsInternal(svc: IBmsService): Boolean {
        return try {
            val cached = svc.getCachedChargingStations()
            val mapped = cached.map { it.toEcoChargingStation() }
            if (mapped.isNotEmpty()) {
                mainHandler.post { onChargingStations(mapped) }
            }
            mapped.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "publishCachedChargingStations failed", e)
            false
        }
    }

    private fun flushPendingRefresh() {
        val pending = pendingRefresh ?: return
        pendingRefresh = null
        binder?.let { invokeRefresh(it, pending.latitude, pending.longitude, pending.radiusMeters) }
    }

    private fun invokeRefresh(
        svc: IBmsService,
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
    ) {
        try {
            svc.refreshChargingStations(null, latitude, longitude, radiusMeters)
        } catch (e: Exception) {
            Log.e(TAG, "requestChargingStationsForDisplay failed", e)
        }
    }

    private fun serviceIntent(): Intent =
        Intent(BMS_SERVICE_ACTION).setPackage(BMS_PACKAGE)

    companion object {
        private const val TAG = "BmsTelemetryBinder"
        const val BMS_PACKAGE = "com.fleet.bms"
        private const val BMS_SERVICE_ACTION = "com.ecocar.bms.action.BMS_SERVICE"
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
