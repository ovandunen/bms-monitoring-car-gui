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
import com.fleet.ecocar.map.EcoChargingStation
import com.fleet.ecocar.telemetry.EcoBmsTelemetry

/**
 * Bindet gegen die Business-Logic-APK (`com.fleet.bms`), registriert [IBmsCallback] und aktualisiert
 * Snapshots auf dem Main-Thread für Compose-[androidx.lifecycle.compose] / StateFlow.
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
            Log.d("BmsTelemetry", "onChargingStationsAvailable: updating map")
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
                svc.getCachedChargingStations()?.let { cached ->
                    mainHandler.post { onChargingStations(cached.map { it.toEcoChargingStation() }) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "registerCallback failed", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }
    }

    fun connect() {
        val intent = Intent(BMS_SERVICE_ACTION).setPackage(BMS_PACKAGE)
        try {
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
        try {
            appContext.unbindService(connection)
        } catch (_: Exception) {
        }
    }

    fun refreshChargingStations(latitude: Double, longitude: Double, radiusMeters: Double = 0.0) {
        try {
            binder?.refreshChargingStations(null, latitude, longitude, radiusMeters)
        } catch (e: Exception) {
            Log.e(TAG, "refreshChargingStations failed", e)
        }
    }

    companion object {
        private const val TAG = "BmsTelemetryBinder"
        const val BMS_PACKAGE = "com.fleet.bms"
        private const val BMS_SERVICE_ACTION = "com.ecocar.bms.action.BMS_SERVICE"
    }
}

private fun ChargingStationSnapshot.toEcoChargingStation() = EcoChargingStation(
    stationId = stationId.orEmpty(),
    displayName = displayName?.takeIf { it.isNotBlank() } ?: stationId.orEmpty(),
    streetAddress = streetAddress,
    city = city,
    latitude = latitude,
    longitude = longitude,
    solarCapacityKw = solarCapacityKw,
    status = status.orEmpty(),
    offlineCache = offlineCache,
)
