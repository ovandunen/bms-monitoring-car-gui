package com.fleet.ecocar.domain.vehicle

import kotlin.test.Test
import kotlin.test.assertEquals

class ChargingStationSelectionPolicyTest {

    @Test
    fun `CSMS advice selects fullest slot mode`() {
        assertEquals(
            ChargingStationSelectionPolicy.Mode.CsmsAdvisedFullest,
            ChargingStationSelectionPolicy.mode(csmsAdvised = true),
        )
    }

    @Test
    fun `low SOC without CSMS advice selects nearest station`() {
        assertEquals(
            ChargingStationSelectionPolicy.Mode.NearestAny,
            ChargingStationSelectionPolicy.mode(csmsAdvised = false),
        )
    }
}
