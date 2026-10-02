package com.fleet.ecocar.ui.battery

import com.fleet.ecocar.domain.vehicle.LadestationSocPolicy
import com.fleet.shared.battery.ui.BatteryOverviewUiModel
import com.fleet.shared.bms.ipc.domain.BatterySnapshot
import com.fleet.shared.bms.ipc.domain.ConnectionStatus

internal fun BatterySnapshot.toOverviewUiModel(
    connection: ConnectionStatus,
    labels: BatteryOverviewLabels,
    lowBatteryPercent: Float,
): BatteryOverviewUiModel {
    val hasLiveData = timestamp > 0L
    val powerKw = if (hasLiveData && (totalVoltage > 0f || current != 0f)) {
        (totalVoltage * current) / 1000f
    } else {
        null
    }
    val hint = when {
        batteryDataStale -> ""
        else -> when (connection) {
            is ConnectionStatus.Connected -> labels.liveHint
            is ConnectionStatus.Connecting -> labels.connectingHint
            is ConnectionStatus.BmsOffline -> labels.offlineHint
            is ConnectionStatus.Error -> connection.reason
            ConnectionStatus.Disconnected -> labels.offlineHint
        }
    }
    return BatteryOverviewUiModel(
        socPercent = stateOfChargePercent.takeIf { hasLiveData },
        packVoltageV = totalVoltage.takeIf { hasLiveData && totalVoltage > 0f },
        packCurrentA = current.takeIf { hasLiveData },
        powerKw = powerKw,
        batteryTempAvgC = batteryTempAvg.takeIf { hasLiveData && totalVoltage > 0f },
        screenTitle = labels.screenTitle,
        socLabel = labels.socLabel,
        voltageLabel = labels.voltageLabel,
        currentLabel = labels.currentLabel,
        powerLabel = labels.powerLabel,
        temperatureLabel = labels.temperatureLabel,
        statusHint = hint,
        showProgress = hasLiveData && !batteryDataStale && stateOfChargePercent in 1f..99f,
        progress = stateOfChargePercent.takeIf { hasLiveData },
        socIsLow = LadestationSocPolicy.isLowSoc(
            stateOfChargePercent,
            hasLiveData,
            lowBatteryPercent,
        ),
        vehicleStatusLabel = vehicleStatusText(
            vehicleStatus,
            labels.driving,
            labels.standby,
            labels.charging,
        ),
        vehicleStatusTitle = labels.vehicleStatusTitle,
        cloudStatusLabel = if (cloudConnected) labels.cloudOnline else labels.cloudOffline,
        metricsStale = batteryDataStale,
        showNoBatteryDataChip = batteryDataStale,
        showOfflineChip = !cloudConnected,
        noBatteryDataLabel = labels.noBatteryData,
        offlineLabel = labels.cloudOffline,
    )
}

internal data class BatteryOverviewLabels(
    val screenTitle: String,
    val socLabel: String,
    val voltageLabel: String,
    val currentLabel: String,
    val powerLabel: String,
    val temperatureLabel: String,
    val liveHint: String,
    val demoHint: String,
    val connectingHint: String,
    val offlineHint: String,
    val driving: String,
    val standby: String,
    val charging: String,
    val vehicleStatusTitle: String,
    val cloudOnline: String,
    val cloudOffline: String,
    val noBatteryData: String,
)
