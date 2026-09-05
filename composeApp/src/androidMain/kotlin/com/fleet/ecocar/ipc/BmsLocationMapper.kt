package com.fleet.ecocar.ipc

import android.location.Location
import com.bms.monitor.aidl.BmsData
import com.bms.monitor.aidl.BmsVehicleLocation

object BmsLocationMapper {

    private const val LOCATION_PROVIDER = "bms_ipc"

    /** From the legacy path: BmsData's optional lat/lon fields. */
    fun toLocation(data: BmsData): Location? {
        val latitude = data.latitude ?: return null
        val longitude = data.longitude ?: return null
        return Location(LOCATION_PROVIDER).apply {
            this.latitude = latitude
            this.longitude = longitude
            time = data.timestamp
        }
    }

    /** From the dedicated onLocationChanged callback - always has a fix,
     * so unlike the BmsData overload above, this never returns null. */
    fun toLocation(location: BmsVehicleLocation): Location =
        Location(LOCATION_PROVIDER).apply {
            latitude = location.latitude
            longitude = location.longitude
            time = location.timestampMillis
            location.accuracyMeters?.let { accuracy = it }
        }
}