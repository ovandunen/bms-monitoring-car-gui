package com.fleet.ecocar.ipc

import android.os.Parcel
import android.os.Parcelable
import com.bms.monitor.aidl.BmsData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class BmsDataSensorParcelTest {

    @Test
    fun parcelRoundTrip_matchesBmsAppFieldOrder() {
        val written = sample()
        val parcel = Parcel.obtain()
        written.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val read = creator().createFromParcel(parcel)
        parcel.recycle()

        assertEquals(written.timestamp, read.timestamp)
        assertTrue(written.cellVoltages.contentEquals(read.cellVoltages))
        assertEquals(written.packTemperature, read.packTemperature)
        assertEquals(written.packHumidity, read.packHumidity)
        assertEquals(written.pm25, read.pm25)
        assertEquals(written.pm10, read.pm10)
        assertEquals(written.soc, read.soc)
        assertEquals(written.current, read.current)
        assertEquals(written.ambientTemperatureC, read.ambientTemperatureC)
        assertEquals(written.humidity, read.humidity)
        assertEquals(written.sensorNodeId, read.sensorNodeId)
        assertEquals(written.sensorTsMs, read.sensorTsMs)
        assertEquals(written.latitude, read.latitude)
        assertEquals(written.longitude, read.longitude)
        assertTrue(read.sensorError)
        assertTrue(read.ds18b20Error)
        assertFalse(read.sht31Error)
        assertTrue(read.pms5003Error)
    }

    @Test
    fun onDataUpdate_mapsOutsideAirHumidityAndSensorError() {
        val snap = sample().toEcoBmsTelemetry()
        assertEquals(34.2f, snap.ambientTemperatureC)
        assertEquals(38.1f, snap.humidity)
        assertTrue(snap.sensorError)
        assertTrue(snap.ds18b20Error)
        assertFalse(snap.sht31Error)
        assertTrue(snap.pms5003Error)
        assertEquals(21f, snap.packTemperature)
        assertEquals(44f, snap.packHumidity)
        assertEquals(87, snap.pm25)
    }

    @Suppress("UNCHECKED_CAST")
    private fun creator(): Parcelable.Creator<BmsData> =
        BmsData::class.java.getField("CREATOR").get(null) as Parcelable.Creator<BmsData>

    private fun sample() = BmsData(
        timestamp = 1000L,
        cellVoltages = floatArrayOf(3.2f, 3.3f),
        packTemperature = 21f,
        packHumidity = 44f,
        pm25 = 87,
        pm10 = 142,
        soc = 80f,
        current = 4f,
        ambientTemperatureC = 34.2f,
        humidity = 38.1f,
        sensorNodeId = "esp32-hub-01",
        sensorTsMs = 1000L,
        latitude = 48.8695,
        longitude = 2.381,
        sensorError = true,
        ds18b20Error = true,
        sht31Error = false,
        pms5003Error = true,
    )
}
