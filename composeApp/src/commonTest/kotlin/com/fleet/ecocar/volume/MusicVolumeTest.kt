package com.fleet.ecocar.volume

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MusicVolumeTest {

    @Test
    fun sliderFraction_mapsEndsAndMiddle() {
        assertEquals(0f, sliderFraction(0, 15))
        assertEquals(1f, sliderFraction(15, 15))
        assertTrue(abs(sliderFraction(7, 15) - (7f / 15f)) < 0.0001f)
        assertEquals(0f, sliderFraction(3, 0))
    }

    @Test
    fun indexFromSlider_roundTripForEveryIndex() {
        val max = 15
        for (index in 0..max) {
            assertEquals(index, indexFromSlider(sliderFraction(index, max), max))
        }
        assertEquals(0, indexFromSlider(0f, 0))
        assertEquals(10, indexFromSlider(1f, 10))
        assertEquals(0, indexFromSlider(-1f, 10))
        assertEquals(10, indexFromSlider(2f, 10))
    }

    @Test
    fun steppedVolumeIndex_clampsAtZeroAndMax() {
        assertEquals(0, steppedVolumeIndex(0, -1, 15))
        assertEquals(1, steppedVolumeIndex(0, 1, 15))
        assertEquals(15, steppedVolumeIndex(15, 1, 15))
        assertEquals(14, steppedVolumeIndex(15, -1, 15))
        assertEquals(0, steppedVolumeIndex(4, -1, 0))
    }

    @Test
    fun toggleMute_setsStreamToZeroAndRestoresTheSameIndex() {
        val audible = MediaVolume(index = 8, maxIndex = 15, muted = false, restoreIndex = 8)
        val muted = audible.toggleMute()
        assertEquals(0, muted.index)
        assertTrue(muted.muted)
        assertEquals(8, muted.restoreIndex)
        val restored = muted.toggleMute()
        assertEquals(8, restored.index)
        assertFalse(restored.muted)
    }

    @Test
    fun chosenZero_keepsThePreviousIndexForUnmute() {
        val dropped = MediaVolume(index = 5, maxIndex = 15, muted = false, restoreIndex = 5)
            .withChosenIndex(0)
        assertTrue(dropped.muted)
        assertEquals(5, dropped.restoreIndex)
        assertEquals(5, dropped.toggleMute().index)
    }

    @Test
    fun observedZero_doesNotForgetTheRestoreIndex() {
        val muted = MediaVolume(index = 8, maxIndex = 15, muted = false, restoreIndex = 8)
            .toggleMute()
            .withObservedIndex(observed = 0, observedMax = 15)
        assertEquals(8, muted.restoreIndex)
        assertTrue(muted.muted)
        val raised = muted.withObservedIndex(observed = 3, observedMax = 15)
        assertFalse(raised.muted)
        assertEquals(3, raised.index)
    }
}
