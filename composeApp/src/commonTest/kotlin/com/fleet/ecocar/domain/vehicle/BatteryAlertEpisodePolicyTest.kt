package com.fleet.ecocar.domain.vehicle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BatteryAlertEpisodePolicyTest {

    @Test
    fun `no alert when SOC above 20 percent`() {
        val (state, event) = BatteryAlertEpisodePolicy.evaluate(
            socPercent = 25f,
            state = BatteryAlertEpisodePolicy.EpisodeState(),
        )
        assertNull(event)
        assertEquals(false, state.stufe2Shown)
        assertEquals(false, state.stufe3Shown)
    }

    @Test
    fun `stufe 2 once when SOC drops to 20 percent`() {
        var state = BatteryAlertEpisodePolicy.EpisodeState()
        val first = BatteryAlertEpisodePolicy.evaluate(20f, state)
        assertEquals(BatteryAlertEpisodePolicy.AlertEvent.ShowStufe2, first.second)
        state = first.first

        val second = BatteryAlertEpisodePolicy.evaluate(18f, state)
        assertNull(second.second)
    }

    @Test
    fun `stufe 2 episode resets when SOC recovers above 20 percent`() {
        var state = BatteryAlertEpisodePolicy.EpisodeState()
        state = BatteryAlertEpisodePolicy.evaluate(15f, state).first
        state = BatteryAlertEpisodePolicy.evaluate(21f, state).first
        val again = BatteryAlertEpisodePolicy.evaluate(19f, state)
        assertEquals(BatteryAlertEpisodePolicy.AlertEvent.ShowStufe2, again.second)
    }

    @Test
    fun `stufe 3 takes priority at 5 percent not stufe 2`() {
        val (state, event) = BatteryAlertEpisodePolicy.evaluate(
            socPercent = 5f,
            state = BatteryAlertEpisodePolicy.EpisodeState(),
        )
        assertEquals(BatteryAlertEpisodePolicy.AlertEvent.ShowStufe3, event)
        assertEquals(true, state.stufe3Shown)
        assertEquals(true, state.stufe2Shown)
    }

    @Test
    fun `stufe 3 once per episode under 5 percent`() {
        var state = BatteryAlertEpisodePolicy.EpisodeState()
        state = BatteryAlertEpisodePolicy.evaluate(4f, state).first
        val repeat = BatteryAlertEpisodePolicy.evaluate(3f, state)
        assertNull(repeat.second)
    }

    @Test
    fun `stufe 3 episode resets when SOC recovers above 5 percent`() {
        var state = BatteryAlertEpisodePolicy.EpisodeState()
        state = BatteryAlertEpisodePolicy.evaluate(4f, state).first
        state = BatteryAlertEpisodePolicy.evaluate(6f, state).first
        val again = BatteryAlertEpisodePolicy.evaluate(5f, state)
        assertEquals(BatteryAlertEpisodePolicy.AlertEvent.ShowStufe3, again.second)
    }

    @Test
    fun `drop from 15 to 4 shows stufe 3 after stufe 2 was already shown`() {
        var state = BatteryAlertEpisodePolicy.EpisodeState()
        state = BatteryAlertEpisodePolicy.evaluate(15f, state).first
        val critical = BatteryAlertEpisodePolicy.evaluate(4f, state)
        assertEquals(BatteryAlertEpisodePolicy.AlertEvent.ShowStufe3, critical.second)
    }

    @Test
    fun `direct drop to 4 percent shows only stufe 3`() {
        val (_, event) = BatteryAlertEpisodePolicy.evaluate(
            socPercent = 4f,
            state = BatteryAlertEpisodePolicy.EpisodeState(),
        )
        assertEquals(BatteryAlertEpisodePolicy.AlertEvent.ShowStufe3, event)
    }
}
