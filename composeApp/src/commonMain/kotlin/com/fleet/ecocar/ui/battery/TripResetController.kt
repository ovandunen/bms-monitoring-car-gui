package com.fleet.ecocar.ui.battery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class TripResetController(
    private val resetTrip: () -> Unit,
) {
    var confirmationVisible by mutableStateOf(false)
        private set

    fun onResetButtonClick() {
        confirmationVisible = true
    }

    fun onConfirmNo() {
        confirmationVisible = false
    }

    fun onConfirmYes() {
        if (!confirmationVisible) return
        confirmationVisible = false
        resetTrip()
    }
}
