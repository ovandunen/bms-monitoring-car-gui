package com.fleet.shared.bms.ipc.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class AidlBatteryServiceAdapterAlertTest {

    @Test
    fun publishAlert_deliversToRegisteredCallback() {
        val adapter = AidlBatteryServiceAdapter()
        val levels = mutableListOf<Int>()
        val messages = mutableListOf<String>()
        adapter.registerCallback(
            object : com.fleet.shared.bms.ipc.IBmsCallback.Stub() {
                override fun onStateChanged(
                    snapshot: com.fleet.shared.bms.ipc.ParcelableBatterySnapshot?,
                ) = Unit

                override fun onConnectionStatusChanged(statusCode: Int) = Unit

                override fun onAlert(level: Int, message: String?) {
                    levels += level
                    messages += message.orEmpty()
                }
            },
        )

        adapter.publishAlert(2, "Low battery")

        assertEquals(listOf(2), levels)
        assertEquals(listOf("Low battery"), messages)
    }
}
