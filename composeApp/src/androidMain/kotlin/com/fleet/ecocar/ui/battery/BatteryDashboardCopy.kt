package com.fleet.ecocar.ui.battery

import com.fleet.shared.bms.ipc.IpcContract
import com.fleet.shared.bms.ipc.VehicleStatus

internal fun ipcErrorPanelMessage(reason: String, versionMismatchMessage: String): String =
    if (reason == IpcContract.VERSION_MISMATCH_REASON) {
        versionMismatchMessage
    } else {
        "Error: $reason"
    }

internal fun vehicleStatusText(
    status: Int,
    driving: String,
    standby: String,
    charging: String,
): String =
    when (status) {
        VehicleStatus.DRIVING -> driving
        VehicleStatus.CHARGING -> charging
        else -> standby
    }
