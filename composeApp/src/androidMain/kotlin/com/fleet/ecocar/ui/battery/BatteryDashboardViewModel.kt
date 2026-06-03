package com.fleet.ecocar.ui.battery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.fleet.ecocar.EcoCarApplication
import com.fleet.shared.bms.ipc.domain.BatterySnapshot
import com.fleet.shared.bms.ipc.domain.CommandType
import com.fleet.shared.bms.ipc.domain.ConnectionStatus
import com.fleet.shared.bms.ipc.domain.bmsCommand
import com.fleet.shared.bms.ipc.infrastructure.AidlBatteryClientAdapter
import kotlinx.coroutines.flow.StateFlow

/**
 * Bridges UI to [AidlBatteryClientAdapter] flows only — no Android Service types here.
 */
class BatteryDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val client: AidlBatteryClientAdapter =
        (application as EcoCarApplication).batteryClient

    val batteryState: StateFlow<BatterySnapshot?> = client.batteryState
    val connectionStatus: StateFlow<ConnectionStatus> = client.connectionStatus

    fun sendCommand(type: CommandType) {
        client.sendCommand(bmsCommand(type))
    }

    fun wakeBms() {
        sendCommand(CommandType.START_MONITORING)
        client.connect()
    }
}
