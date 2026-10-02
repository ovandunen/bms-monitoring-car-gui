package com.fleet.shared.battery.ui

/**
 * Driver-facing battery overview state (domain DTO for the shared UI port).
 * Callers map from CAN telemetry, demo snapshots, or IPC — this module stays agnostic.
 */
data class BatteryOverviewUiModel(
    val socPercent: Float?,
    val packVoltageV: Float?,
    val packCurrentA: Float?,
    val powerKw: Float?,
    val batteryTempAvgC: Float?,
    val screenTitle: String,
    val socLabel: String,
    val voltageLabel: String,
    val currentLabel: String,
    val powerLabel: String,
    val temperatureLabel: String,
    val statusHint: String,
    val showProgress: Boolean = false,
    val progress: Float? = null,
    val socIsLow: Boolean = false,
    val vehicleStatusLabel: String = "",
    val vehicleStatusTitle: String = "",
    val cloudStatusLabel: String = "",
    val metricsStale: Boolean = false,
    val showNoBatteryDataChip: Boolean = false,
    val showOfflineChip: Boolean = false,
    val noBatteryDataLabel: String = "",
    val offlineLabel: String = "",
)
