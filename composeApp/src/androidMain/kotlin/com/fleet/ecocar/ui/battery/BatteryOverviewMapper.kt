package com.fleet.ecocar.ui.battery

import com.fleet.shared.battery.ui.BatteryOverviewUiModel
import com.fleet.shared.bms.ipc.domain.BatterySnapshot
import com.fleet.shared.bms.ipc.domain.ConnectionStatus

internal fun BatterySnapshot.toOverviewUiModel(
    connection: ConnectionStatus,
    labels: BatteryOverviewLabels,
): BatteryOverviewUiModel {
    val powerKw = (totalVoltage * current) / 1000f
    val hint = when (connection) {
        is ConnectionStatus.Connected -> labels.liveHint
        is ConnectionStatus.Connecting -> labels.connectingHint
        is ConnectionStatus.BmsOffline -> labels.offlineHint
        is ConnectionStatus.Error -> connection.reason
        ConnectionStatus.Disconnected -> labels.offlineHint
    }
    return BatteryOverviewUiModel(
        socPercent = stateOfChargePercent,
        packVoltageV = totalVoltage,
        packCurrentA = current,
        powerKw = powerKw,
        screenTitle = labels.screenTitle,
        socLabel = labels.socLabel,
        voltageLabel = labels.voltageLabel,
        currentLabel = labels.currentLabel,
        powerLabel = labels.powerLabel,
        statusHint = hint,
        snifferButtonLabel = labels.snifferLabel,
        showProgress = stateOfChargePercent in 1f..99f,
        progress = stateOfChargePercent / 100f,
    )
}

internal data class BatteryOverviewLabels(
    val screenTitle: String,
    val socLabel: String,
    val voltageLabel: String,
    val currentLabel: String,
    val powerLabel: String,
    val liveHint: String,
    val demoHint: String,
    val connectingHint: String,
    val offlineHint: String,
    val snifferLabel: String,
)
