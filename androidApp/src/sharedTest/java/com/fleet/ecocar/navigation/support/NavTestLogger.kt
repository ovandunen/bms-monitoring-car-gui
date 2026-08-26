package com.fleet.ecocar.navigation.support

import android.util.Log

/** Structured JSON lines for `adb logcat -s NavTest:*`. */
object NavTestLogger {

    const val TAG = "NavTest"

    fun logLocationUpdate(lat: Double, lon: Double, distanceToStationM: Double?) {
        Log.d(
            TAG,
            """{"event":"location_update","lat":$lat,"lon":$lon,"distanceToStationM":${distanceToStationM ?: "null"},"ts":${System.currentTimeMillis()}}""",
        )
    }

    fun logManeuver(instruction: String) {
        Log.d(
            TAG,
            """{"event":"maneuver","instruction":"${instruction.replace("\"", "\\\"")}","ts":${System.currentTimeMillis()}}""",
        )
    }

    fun logStationReached(stationId: String) {
        Log.d(
            TAG,
            """{"event":"station_reached","stationId":"$stationId","ts":${System.currentTimeMillis()}}""",
        )
    }
}
