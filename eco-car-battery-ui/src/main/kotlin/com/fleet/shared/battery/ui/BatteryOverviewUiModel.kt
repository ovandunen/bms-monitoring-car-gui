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
    val screenTitle: String,
    val socLabel: String,
    val voltageLabel: String,
    val currentLabel: String,
    val powerLabel: String,
    val statusHint: String,
    val snifferButtonLabel: String,
    val showProgress: Boolean = false,
    val progress: Float? = null,
)
