package com.fleet.ecocar.ui.battery

import com.fleet.ecocar.telemetry.EcoBmsAlert
import kotlin.test.Test
import kotlin.test.assertEquals

class BmsAlertToSubNavTest {

    @Test
    fun onAlert_appendsAndMapsToBatterySubNavAlerts() {
        val fromBinder = emptyList<EcoBmsAlert>() + EcoBmsAlert(level = 2, message = "Low battery: 12.0% remaining")
        val forSubNav = fromBinder.map { it.toUiAlert() }

        assertEquals(1, forSubNav.size)
        assertEquals(DemoAlertSeverity.WARNING, forSubNav.single().severity)
        assertEquals("Low battery: 12.0% remaining", forSubNav.single().message)
    }

    @Test
    fun ipcCriticalLevel_mapsToCritical() {
        val alert = EcoBmsAlert(level = 3, message = "Critical: Battery 4.0%").toUiAlert()
        assertEquals(DemoAlertSeverity.CRITICAL, alert.severity)
    }
}
