package com.fleet.ecocar.ui.battery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripResetControllerTest {

    @Test
    fun resetButton_opensDialog_noDoesNothing_yesResetsOnce() {
        var resetCalls = 0
        val controller = TripResetController { resetCalls++ }

        controller.onResetButtonClick()
        assertTrue(controller.confirmationVisible)
        assertEquals(0, resetCalls)

        controller.onConfirmNo()
        assertFalse(controller.confirmationVisible)
        assertEquals(0, resetCalls)

        controller.onResetButtonClick()
        controller.onConfirmYes()
        assertFalse(controller.confirmationVisible)
        assertEquals(1, resetCalls)
    }

    @Test
    fun confirmYesWithoutDialog_doesNotReset() {
        var resetCalls = 0
        val controller = TripResetController { resetCalls++ }
        controller.onConfirmYes()
        assertEquals(0, resetCalls)
    }
}
