package com.fleet.ecocar.volume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build

/**
 * STREAM_MUSIC via [AudioManager.setStreamVolume] with flags 0 (no system panel).
 * [Settings.System.VOLUME_MUSIC] is not in this compile SDK, so hardware and
 * other-app changes are observed with the system broadcast
 * android.media.VOLUME_CHANGED_ACTION.
 */
class AndroidMusicVolume(
    private val context: Context,
    private val audioManager: AudioManager,
) : MusicVolumeControl {

    override fun read(): MusicVolumeLevel {
        val maxIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val index = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).coerceIn(0, maxIndex)
        return MusicVolumeLevel(index, maxIndex)
    }

    override fun setIndex(index: Int) {
        val maxIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            index.coerceIn(0, maxIndex),
            0,
        )
    }

    override fun observe(onChange: (MusicVolumeLevel) -> Unit): MusicVolumeSubscription {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val stream = intent?.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) ?: -1
                if (stream == AudioManager.STREAM_MUSIC) {
                    onChange(read())
                }
            }
        }
        val filter = IntentFilter(VOLUME_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        return MusicVolumeSubscription {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    private companion object {
        const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
        const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"
    }
}
