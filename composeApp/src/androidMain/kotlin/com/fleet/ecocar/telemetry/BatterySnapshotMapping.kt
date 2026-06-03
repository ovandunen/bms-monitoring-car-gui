package com.fleet.ecocar.telemetry

import com.fleet.ecocar.ui.battery.CELL_COUNT
import com.fleet.shared.bms.ipc.domain.BatterySnapshot

internal fun BatterySnapshot.toEcoBmsTelemetry(): EcoBmsTelemetry =
    EcoBmsTelemetry(
        timestamp = timestamp,
        cellVolts = syntheticCellVolts(),
        packTemperature = batteryTempMax.toFloat(),
        packHumidity = 0f,
        pm25 = 0,
        pm10 = 0,
        soc = stateOfChargePercent,
        currentA = current,
    )

/** Approximate per-cell voltages from CAN min/max (mV) when a full array is not on the IPC wire. */
private fun BatterySnapshot.syntheticCellVolts(): List<Float> {
    if (cellVoltageMax <= 0 && cellVoltageMin <= 0) return emptyList()
    val maxV = cellVoltageMax / 1000f
    val minV = cellVoltageMin / 1000f
    if (maxV <= 0f && minV <= 0f) return emptyList()
    if (kotlin.math.abs(maxV - minV) < 0.001f) {
        return List(CELL_COUNT) { maxV.coerceAtLeast(minV) }
    }
    val steps = (CELL_COUNT - 1).coerceAtLeast(1)
    return List(CELL_COUNT) { i ->
        minV + (maxV - minV) * i / steps
    }
}
