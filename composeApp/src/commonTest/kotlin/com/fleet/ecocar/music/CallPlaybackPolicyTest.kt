package com.fleet.ecocar.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CallPlaybackPolicyTest {

    @Test
    fun playingCallPausesAndShowsBanner() {
        val policy = CallPlaybackPolicy()
        assertTrue(policy.onCallInterrupted(wasPlaying = true, showBanner = true))
        assertTrue(policy.bannerVisible)
    }

    @Test
    fun alreadyPausedCallDoesNotPauseAgain() {
        val policy = CallPlaybackPolicy()
        assertFalse(policy.onCallInterrupted(wasPlaying = false, showBanner = true))
        assertFalse(policy.bannerVisible)
    }

    @Test
    fun callEndArmsFourSecondResumeAndKeepsTheBanner() {
        val policy = CallPlaybackPolicy()
        policy.onCallInterrupted(wasPlaying = true, showBanner = true)
        assertEquals(CallPlaybackPolicy.RESUME_AFTER_CALL_MS, policy.onCallFinished(stillInCall = false))
        assertTrue(policy.bannerVisible)
        assertTrue(policy.onResumeDelayElapsed())
        assertFalse(policy.bannerVisible)
    }

    @Test
    fun finishWhileStillInCallDoesNotResume() {
        val policy = CallPlaybackPolicy()
        policy.onCallInterrupted(wasPlaying = true, showBanner = true)
        assertEquals(null, policy.onCallFinished(stillInCall = true))
        assertTrue(policy.bannerVisible)
        assertEquals(CallPlaybackPolicy.RESUME_AFTER_CALL_MS, policy.onCallFinished(stillInCall = false))
    }

    @Test
    fun userResumeClearsTheHoldAndIgnoresTheCallUntilItEnds() {
        val policy = CallPlaybackPolicy()
        policy.onCallInterrupted(wasPlaying = true, showBanner = true)
        policy.onUserResume()
        assertFalse(policy.bannerVisible)
        assertTrue(policy.userOverride)
        assertFalse(policy.onCallInterrupted(wasPlaying = true, showBanner = true))
        assertEquals(null, policy.onCallFinished(stillInCall = false))
        assertFalse(policy.userOverride)
        assertFalse(policy.onResumeDelayElapsed())
    }

    @Test
    fun userPauseCancelsAutoResume() {
        val policy = CallPlaybackPolicy()
        policy.onCallInterrupted(wasPlaying = true, showBanner = true)
        policy.onUserPause()
        assertEquals(null, policy.onCallFinished(stillInCall = false))
        assertFalse(policy.onResumeDelayElapsed())
        assertFalse(policy.bannerVisible)
    }

    @Test
    fun ordinaryPlayDoesNotBlockTheNextCall() {
        val policy = CallPlaybackPolicy()
        policy.onUserResume()
        assertFalse(policy.userOverride)
        assertTrue(policy.onCallInterrupted(wasPlaying = true, showBanner = true))
    }
}
