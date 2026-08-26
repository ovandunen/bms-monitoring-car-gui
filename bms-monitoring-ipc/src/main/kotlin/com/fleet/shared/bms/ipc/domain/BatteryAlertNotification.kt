package com.fleet.shared.bms.ipc.domain

/** Alert pushed from BMS monitor IPC (level 1=info, 2=warning, 3=critical). */
data class BatteryAlertNotification(
    val level: Int,
    val message: String,
    val receivedAtMs: Long = System.currentTimeMillis(),
)
