package com.fleet.ecocar.music

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import androidx.media3.exoplayer.ExoPlayer

/**
 * Pauses STREAM_MUSIC on a call (focus loss or call audio mode). Navigation ducking
 * only lowers the player volume. Resume is one tap, or 4 seconds after the call ends.
 */
class MediaCallController(
    context: Context,
    private val handler: Handler,
    private val player: () -> ExoPlayer?,
    private val publish: () -> Unit,
) {
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val policy = CallPlaybackPolicy()
    private var started = false
    private var ducking = false
    private var focusRequest: AudioFocusRequest? = null

    val bannerVisible: Boolean get() = policy.bannerVisible

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        handleFocus(change)
    }

    private val resumeRunnable = Runnable {
        if (policy.onResumeDelayElapsed()) {
            val current = player()
            if (current != null) {
                ducking = false
                current.volume = 1f
                current.play()
                requestFocus()
            }
        }
        publish()
    }

    fun start() {
        if (started) return
        started = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.addOnModeChangedListener({ command -> handler.post(command) }) { mode ->
                handleMode(mode)
            }
        }
    }

    fun onPlaybackStarted() {
        if (!ducking) player()?.volume = 1f
        requestFocus()
    }

    fun userPause() {
        policy.onUserPause()
        cancelResume()
        ducking = false
        abandonFocus()
        player()?.let {
            it.volume = 1f
            it.pause()
        }
        publish()
    }

    fun userPlay() {
        policy.onUserResume()
        cancelResume()
        ducking = false
        val current = player() ?: return
        current.volume = 1f
        current.play()
        requestFocus()
        publish()
    }

    fun onStopped() {
        policy.onUserPause()
        cancelResume()
        ducking = false
        abandonFocus()
        player()?.volume = 1f
    }

    private fun handleMode(mode: Int) {
        val inCall = isCallAudioMode(mode)
        if (inCall) {
            cancelResume()
            if (policy.onCallInterrupted(wasPlaying = playing(), showBanner = true)) {
                pauseForCall()
            }
        } else {
            armResume(policy.onCallFinished(stillInCall = false))
        }
        publish()
    }

    private fun handleFocus(change: Int) {
        val inCall = isCallAudioMode(audioManager.mode)
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                ducking = false
                player()?.volume = 1f
                armResume(policy.onCallFinished(stillInCall = inCall))
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                ducking = false
                player()?.volume = 1f
                if (inCall) {
                    cancelResume()
                    if (policy.onCallInterrupted(wasPlaying = playing(), showBanner = true)) {
                        pauseForCall()
                    }
                } else if (playing()) {
                    policy.onUserPause()
                    cancelResume()
                    player()?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                ducking = false
                player()?.volume = 1f
                cancelResume()
                if (policy.onCallInterrupted(wasPlaying = playing(), showBanner = true)) {
                    pauseForCall()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (inCall) {
                    ducking = false
                    player()?.volume = 1f
                    cancelResume()
                    if (policy.onCallInterrupted(wasPlaying = playing(), showBanner = true)) {
                        pauseForCall()
                    }
                } else {
                    ducking = true
                    player()?.volume = DUCK_VOLUME
                }
            }
        }
        publish()
    }

    private fun pauseForCall() {
        ducking = false
        player()?.let {
            it.volume = 1f
            it.pause()
        }
    }

    private fun playing(): Boolean {
        val current = player() ?: return false
        return current.isPlaying || current.playWhenReady
    }

    private fun armResume(delayMs: Long?) {
        cancelResume()
        if (delayMs != null) handler.postDelayed(resumeRunnable, delayMs)
    }

    private fun cancelResume() {
        handler.removeCallbacks(resumeRunnable)
    }

    private fun requestFocus() {
        val request = focusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setOnAudioFocusChangeListener(focusListener, handler)
            .setAcceptsDelayedFocusGain(true)
            .setWillPauseWhenDucked(false)
            .build()
            .also { focusRequest = it }
        audioManager.requestAudioFocus(request)
    }

    private fun abandonFocus() {
        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }

    private companion object {
        const val DUCK_VOLUME = 0.2f

        fun isCallAudioMode(mode: Int): Boolean =
            mode == AudioManager.MODE_RINGTONE ||
                mode == AudioManager.MODE_IN_CALL ||
                mode == AudioManager.MODE_IN_COMMUNICATION
    }
}
