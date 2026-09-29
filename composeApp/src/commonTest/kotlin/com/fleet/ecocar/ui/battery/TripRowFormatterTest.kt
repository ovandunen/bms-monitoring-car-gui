package com.fleet.ecocar.ui.battery

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TripRowFormatterTest {

    @Test
    fun runningSession_usesRunningLabel_andHidesConsumptionWhenKmZero() {
        val row = TripRowFormatter.format(
            startedAt = 100L,
            endedAt = TripRowFormatter.OPEN_ENDED_AT,
            distanceKm = 0f,
            energyKwh = 1.5f,
            runningLabel = "running",
        )
        assertEquals("running", row.endText)
        assertEquals("0.0", row.kmText)
        assertEquals("1.50", row.kwhText)
        assertNull(row.consumptionText)
    }

    @Test
    fun closedSession_formatsKmKwhAndConsumption() {
        val row = TripRowFormatter.format(
            startedAt = 100L,
            endedAt = 200L,
            distanceKm = 12.5f,
            energyKwh = 3.25f,
            runningLabel = "running",
        )
        assertEquals("200", row.endText)
        assertEquals("12.5", row.kmText)
        assertEquals("3.25", row.kwhText)
        assertEquals("0.260", row.consumptionText)
    }
}
