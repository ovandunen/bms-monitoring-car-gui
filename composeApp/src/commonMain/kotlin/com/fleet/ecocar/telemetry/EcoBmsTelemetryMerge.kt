package com.fleet.ecocar.telemetry

/**
 * Merges monitor IPC (CAN: SOC, voltage, …) with ESP32 USB sensors (DS18B20 external temp, SHT31, PMS5003).
 * VCU/CAN pack temperature is out of scope — Charts uses [EcoBmsTelemetry.ambientTemperatureC] (DS18B20 outside air).
 */
object EcoBmsTelemetryMerge {

    /** Apply a live IPC snapshot while keeping USB sensor fields from [existing]. */
    fun mergeIpcUpdate(ipc: EcoBmsTelemetry, existing: EcoBmsTelemetry): EcoBmsTelemetry =
        ipc.copy(
            cellVolts = ipc.cellVolts.ifEmpty { existing.cellVolts },
            ambientTemperatureC = existing.ambientTemperatureC.takeIf { it != 0f } ?: ipc.ambientTemperatureC,
            packHumidity = if (ipc.packHumidity == 0f) existing.packHumidity else ipc.packHumidity,
            humidity = if (ipc.humidity == 0f) existing.humidity else ipc.humidity,
            pm25 = if (ipc.pm25 == 0) existing.pm25 else ipc.pm25,
            pm10 = if (ipc.pm10 == 0) existing.pm10 else ipc.pm10,
        )

    /** Overlay USB/legacy BMS sensor readings onto the current telemetry (when monitor IPC is live). */
    fun mergeUsbSensors(current: EcoBmsTelemetry, usb: EcoBmsTelemetry): EcoBmsTelemetry =
        current.copy(
            cellVolts = usb.cellVolts.ifEmpty { current.cellVolts },
            ambientTemperatureC = usb.ambientTemperatureC.takeIf { it != 0f } ?: current.ambientTemperatureC,
            packHumidity = if (current.packHumidity == 0f) usb.packHumidity else current.packHumidity,
            humidity = if (usb.humidity != 0f) usb.humidity else current.humidity,
            pm25 = if (usb.pm25 != 0) usb.pm25 else current.pm25,
            pm10 = if (usb.pm10 != 0) usb.pm10 else current.pm10,
        )
}
