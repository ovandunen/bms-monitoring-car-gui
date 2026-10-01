package com.fleet.ecocar

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process

internal object EcoCarProcessIdentity {

    fun isMainProcess(packageName: String, processName: String): Boolean =
        processName == packageName

    fun currentProcessName(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName()
        }
        val pid = Process.myPid()
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.runningAppProcesses
            ?.firstOrNull { it.pid == pid }
            ?.processName
            ?: context.packageName
    }
}
