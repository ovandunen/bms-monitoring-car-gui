package com.fleet.ecocar.music

/**
 * Real pause while a call holds audio, then one tap or a short delay starts the music again.
 * Ducking (navigation prompts) never comes through here.
 */
class CallPlaybackPolicy {
    var bannerVisible: Boolean = false
        private set

    var userOverride: Boolean = false
        private set

    private var pausedByCall: Boolean = false

    /**
     * @return true when the player should pause now
     */
    fun onCallInterrupted(wasPlaying: Boolean, showBanner: Boolean): Boolean {
        if (userOverride) return false
        if (wasPlaying) {
            pausedByCall = true
            bannerVisible = showBanner
            return true
        }
        if (pausedByCall && showBanner) bannerVisible = true
        return false
    }

    /**
     * Call ended, or focus came back and the device is not in a call.
     * @return milliseconds to wait before auto-resume, or null when nothing is held
     */
    fun onCallFinished(stillInCall: Boolean): Long? {
        if (stillInCall) return null
        userOverride = false
        if (!pausedByCall) {
            bannerVisible = false
            return null
        }
        bannerVisible = true
        return RESUME_AFTER_CALL_MS
    }

    fun onUserResume() {
        val duringHold = pausedByCall || bannerVisible
        pausedByCall = false
        bannerVisible = false
        if (duringHold) userOverride = true
    }

    fun onUserPause() {
        pausedByCall = false
        bannerVisible = false
        userOverride = false
    }

    /** @return true when the delayed resume should start playback */
    fun onResumeDelayElapsed(): Boolean {
        if (!pausedByCall) return false
        pausedByCall = false
        bannerVisible = false
        userOverride = false
        return true
    }

    companion object {
        const val RESUME_AFTER_CALL_MS = 4_000L
    }
}
