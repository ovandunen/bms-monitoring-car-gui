package com.fleet.ecocar.ipc

import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import com.bms.monitor.aidl.BmsVehicleLocation
import com.bms.monitor.aidl.IBmsCallback
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class BmsTelemetryBinderLocationTransactionTest {

    @Test
    fun onLocationChanged_transactionCodePosition5_reachesOnLocationUpdate() {
        val received = mutableListOf<Location>()
        val binder = BmsTelemetryBinder(
            context = RuntimeEnvironment.getApplication(),
            onTelemetry = {},
            onChargingStations = {},
            onLocationUpdate = { received += it },
        )
        val stub = callbackStub(binder)
        val location = BmsVehicleLocation(
            latitude = 52.52,
            longitude = 13.405,
            timestampMillis = 1_700_000_000_000L,
            accuracyMeters = 8.5f,
        )

        val data = Parcel.obtain()
        try {
            data.writeInterfaceToken(IBmsCallback.DESCRIPTOR)
            data.writeInt(1)
            location.writeToParcel(data, 0)
            data.setDataPosition(0)
            val ok = stub.onTransact(
                IBinder.FIRST_CALL_TRANSACTION + 4,
                data,
                null,
                IBinder.FLAG_ONEWAY,
            )
            assertEquals(true, ok)
        } finally {
            data.recycle()
        }

        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, received.size)
        assertEquals(52.52, received[0].latitude, 0.0001)
        assertEquals(13.405, received[0].longitude, 0.0001)
        assertEquals(1_700_000_000_000L, received[0].time)
    }

    private fun callbackStub(binder: BmsTelemetryBinder): IBmsCallback.Stub {
        val field = BmsTelemetryBinder::class.java.getDeclaredField("callback")
        field.isAccessible = true
        return field.get(binder) as IBmsCallback.Stub
    }
}
