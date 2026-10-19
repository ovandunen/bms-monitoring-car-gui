package com.fleet.ecocar.canlog

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

/**
 * Starts the BMS monitor, then sends the same broadcast as `make tablet-can-log-start`.
 * The delay leaves the monitor time to register its receiver when the process was down.
 * A second press sends the broadcast again, and the monitor treats that as stop.
 */
object CanLogButton {
    private const val BMS_PACKAGE = "ch.ecocarsolaire.bms"
    private const val MONITOR_SERVICE = "ch.ecocar.bms.BmsMonitorService"
    private const val MONITOR_ACTION = "ch.ecocarsolaire.bms.action.MONITOR_SERVICE"
    private const val TOGGLE_ACTION = "ch.ecocarsolaire.bms.DEBUG_CAN_LOG_START"
    private const val RECEIVER_WAIT_MS = 800L

    fun press(context: Context) {
        val app = context.applicationContext
        val wake = Intent()
            .setComponent(ComponentName(BMS_PACKAGE, MONITOR_SERVICE))
            .setAction(MONITOR_ACTION)
        app.startForegroundService(wake)
        Handler(Looper.getMainLooper()).postDelayed({
            val toggle = Intent(TOGGLE_ACTION)
                .setPackage(BMS_PACKAGE)
                .putExtra("label", "sniff")
            app.sendBroadcast(toggle)
        }, RECEIVER_WAIT_MS)
    }
}
