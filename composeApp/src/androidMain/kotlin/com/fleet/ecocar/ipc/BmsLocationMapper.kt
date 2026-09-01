package com.fleet.ecocar.ipc

import android.location.Location
import com.bms.monitor.aidl.BmsData

/**
 * Single responsibility: decide whether a [BmsData] reading carries a usable
 * GPS fix and, if so, convert it into an [android.location.Location] -
 * the type [ch.fleet.ecocar.EcoCarApplication.onLocationUpdate] already
 * expects, via [BmsTelemetryBinder].
 *
 * BmsData does not carry altitude/speed/accuracy (BmsService's
 * LocationStampingBmsDataFactory only stamps latitude/longitude onto a
 * reading) - those fields are deliberately left at Location's own defaults
 * (0.0 / not-set) rather than fabricated, per the no-invented-data
 * constraint used throughout this fix.
 */
object BmsLocationMapper {

    private const val LOCATION_PROVIDER = "bms_ipc"

    fun toLocation(data: BmsData): Location? {
        val latitude = data.latitude ?: return null
        val longitude = data.longitude ?: return null
        return Location(LOCATION_PROVIDER).apply {
            this.latitude = latitude
            this.longitude = longitude
            time = data.timestamp
        }
    }
}