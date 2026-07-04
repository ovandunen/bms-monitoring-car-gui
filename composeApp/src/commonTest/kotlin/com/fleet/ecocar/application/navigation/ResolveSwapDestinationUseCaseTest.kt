package com.fleet.ecocar.application.navigation

import com.fleet.ecocar.map.EcoChargingStation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResolveSwapDestinationUseCaseTest {

    private val useCase = ResolveSwapDestinationUseCase()

    @Test
    fun acceptedSwapRecommendation_resolvesStationFromExistingIpcList() {
        val destination = useCase.resolve(
            stationId = "sn-dakar",
            stations = listOf(dakarStation()),
        )

        assertEquals("sn-dakar", destination?.stationId)
        assertEquals(14.7167, destination?.coordinates?.latitude)
        assertEquals(-17.4677, destination?.coordinates?.longitude)
    }

    @Test
    fun unknownStationId_returnsNullWithoutSecondMqttSubscription() {
        assertNull(useCase.resolve("missing", listOf(dakarStation())))
    }

    private fun dakarStation() = EcoChargingStation(
        stationId = "sn-dakar",
        displayName = "Dakar Solar Hub",
        streetAddress = "Route de Rufisque",
        city = "Dakar",
        latitude = 14.7167,
        longitude = -17.4677,
        solarCapacityKw = 120.0,
        status = "AVAILABLE",
        offlineCache = false,
    )
}
