package com.fleet.ecocar.ui.bottom

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.fleet.shared.bms.ipc.domain.BatterySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TripResetViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun resetTripDistance_dispatchesToBatteryPort() {
        val port = FakeBottomBarBatteryPort(sampleSnapshot(tripDistanceKm = 71f))
        val viewModel = BottomBarViewModel(port, FakeHintRepository(shown = true))
        awaitTelemetry(viewModel)

        viewModel.resetTripDistance()

        assertEquals(1, port.resetTripCalls)
    }

    @Test
    fun resetTripDistance_doesNotMutateLocalTelemetryBeforeNextIpcUpdate() {
        val port = FakeBottomBarBatteryPort(sampleSnapshot(tripDistanceKm = 71f))
        val viewModel = BottomBarViewModel(port, FakeHintRepository(shown = true))
        awaitTelemetry(viewModel)

        viewModel.resetTripDistance()

        assertEquals(71, viewModel.telemetry.value.tripDistanceKm)
    }

    @Test
    fun ipcUpdateWithZeroTripDistance_updatesDisplayedValue() {
        val port = FakeBottomBarBatteryPort(sampleSnapshot(tripDistanceKm = 71f))
        val viewModel = BottomBarViewModel(port, FakeHintRepository(shown = true))
        awaitTelemetry(viewModel)
        assertEquals(71, viewModel.telemetry.value.tripDistanceKm)

        port.emit(sampleSnapshot(tripDistanceKm = 0f))
        awaitTelemetry(viewModel)

        assertNull(viewModel.telemetry.value.tripDistanceKm)
    }

    @Test
    fun subHalfKmTrip_showsNullUntilHalfKm() {
        val port = FakeBottomBarBatteryPort(sampleSnapshot(tripDistanceKm = 0.3f))
        val viewModel = BottomBarViewModel(port, FakeHintRepository(shown = true))
        awaitTelemetry(viewModel)

        assertNull(viewModel.telemetry.value.tripDistanceKm)
    }

    @Test
    fun noLiveSnapshot_showsNullTrip() {
        val port = FakeBottomBarBatteryPort(sampleSnapshot(tripDistanceKm = 71f).copy(timestamp = 0L))
        val viewModel = BottomBarViewModel(port, FakeHintRepository(shown = true))
        awaitTelemetry(viewModel)
        assertNull(viewModel.telemetry.value.tripDistanceKm)
    }

    private fun awaitTelemetry(viewModel: BottomBarViewModel) {
        runBlocking {
            kotlinx.coroutines.delay(50)
        }
    }

    private fun sampleSnapshot(tripDistanceKm: Float) = BatterySnapshot(
        timestamp = 1L,
        stateOfChargePercent = 50f,
        totalVoltage = 310f,
        current = -9.8f,
        cellVoltageMax = 4100,
        cellVoltageMin = 3200,
        batteryTempMax = 25,
        batteryTempMin = 20,
        controllerTemp = 0,
        motorTemp = 0,
        motorRpm = 0,
        vehicleSpeed = 0f,
        tripDistanceKm = tripDistanceKm,
    )

    private class FakeBottomBarBatteryPort(
        snapshot: BatterySnapshot?,
    ) : BottomBarBatteryPort {
        private val state = MutableStateFlow(snapshot)
        override val batteryState: StateFlow<BatterySnapshot?> = state
        var resetTripCalls = 0

        override fun resetTrip() {
            resetTripCalls++
        }

        fun emit(snapshot: BatterySnapshot?) {
            state.value = snapshot
        }
    }

    private class FakeHintRepository(
        private val shown: Boolean,
    ) : TripResetHintRepository {
        override suspend fun isHintShown(): Boolean = shown
        override suspend fun markHintShown() = Unit
    }
}
