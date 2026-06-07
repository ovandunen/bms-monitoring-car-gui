package com.fleet.ecocar.ui.bottom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fleet.shared.bms.ipc.domain.BatterySnapshot
import com.fleet.shared.bms.ipc.infrastructure.AidlBatteryClientAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import timber.log.Timber

/** IPC port for bottom-bar telemetry — no local trip state. */
interface BottomBarBatteryPort {
    val batteryState: StateFlow<BatterySnapshot?>
    fun resetTrip()
}

fun AidlBatteryClientAdapter.asBottomBarBatteryPort(): BottomBarBatteryPort =
    object : BottomBarBatteryPort {
        override val batteryState = this@asBottomBarBatteryPort.batteryState
        override fun resetTrip() = this@asBottomBarBatteryPort.resetTrip()
    }

class BottomBarViewModel(
    private val batteryPort: BottomBarBatteryPort,
    private val hintRepository: TripResetHintRepository,
) : ViewModel() {

    private val _telemetry = MutableStateFlow(BottomTelemetry())
    val telemetry: StateFlow<BottomTelemetry> = _telemetry.asStateFlow()

    private val _showTripResetHint = MutableStateFlow(false)
    val showTripResetHint: StateFlow<Boolean> = _showTripResetHint.asStateFlow()

    init {
        viewModelScope.launch {
            if (!hintRepository.isHintShown()) {
                _showTripResetHint.value = true
            }
        }
        viewModelScope.launch {
            batteryPort.batteryState.collect { snapshot ->
                publishTelemetry(snapshot)
            }
        }
    }

    fun resetTripDistance() {
        batteryPort.resetTrip()
        Timber.d("EcoCarViewModel: resetTripDistance() dispatched to BMS")
    }

    fun dismissTripResetHint() {
        _showTripResetHint.value = false
        viewModelScope.launch { hintRepository.markHintShown() }
    }

    private fun publishTelemetry(snapshot: BatterySnapshot?) {
        val live = snapshot?.takeIf { it.timestamp > 0L }
        _telemetry.value = BottomTelemetry(
            socPercent = live?.stateOfChargePercent?.toInt() ?: 0,
            tripDistanceKm = live?.tripDistanceKm?.takeIf { it >= 0.5f }?.roundToInt(),
            rangeKm = live?.estimatedRangeKm?.takeIf { it > 0f }?.toDouble(),
            co2SavingKg = live?.co2SavingKg?.takeIf { live.tripDistanceKm >= 0.5f }?.toDouble(),
        )
    }
}
