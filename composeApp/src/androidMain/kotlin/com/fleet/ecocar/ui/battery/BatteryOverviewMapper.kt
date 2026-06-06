package com.fleet.ecocar.ui.battery

import com.fleet.ecocar.domain.vehicle.LadestationSocPolicy
import com.fleet.shared.battery.ui.BatteryOverviewUiModel
import com.fleet.shared.bms.ipc.domain.BatterySnapshot
import com.fleet.shared.bms.ipc.domain.ConnectionStatus

internal fun BatterySnapshot.toOverviewUiModel(
    connection: ConnectionStatus,
    labels: BatteryOverviewLabels,
): BatteryOverviewUiModel {
    val hasLiveData = timestamp > 0L
    val powerKw = if (hasLiveData && (totalVoltage > 0f || current != 0f)) {
        (totalVoltage * current) / 1000f
    } else {
        null
    }
    val hint = when (connection) {
        is ConnectionStatus.Connected -> labels.liveHint
        is ConnectionStatus.Connecting -> labels.connectingHint
        is ConnectionStatus.BmsOffline -> labels.offlineHint
        is ConnectionStatus.Error -> connection.reason
        ConnectionStatus.Disconnected -> labels.offlineHint
    }
    return BatteryOverviewUiModel(
        socPercent = stateOfChargePercent.takeIf { hasLiveData },
        packVoltageV = totalVoltage.takeIf { hasLiveData && totalVoltage > 0f },
        packCurrentA = current.takeIf { hasLiveData },
        powerKw = powerKw,
        screenTitle = labels.screenTitle,
        socLabel = labels.socLabel,
        voltageLabel = labels.voltageLabel,
        currentLabel = labels.currentLabel,
        powerLabel = labels.powerLabel,
        statusHint = hint,
        showProgress = hasLiveData && stateOfChargePercent in 1f..99f,
        progress = stateOfChargePercent.takeIf { hasLiveData },
        socIsLow = hasLiveData &&
            stateOfChargePercent in 0.01f..<LadestationSocPolicy.LOW_BATTERY_PERCENT,
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
)
