package com.fleet.ecocar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Foreground [MediaSessionService] so playback continues when switching dashboard tabs.
 * Uses the application-scoped [ExoPlayer] from [EcoCarApplication].
 */
class MusicPlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Musik",
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
        val app = application as EcoCarApplication
        val player = app.ensureMusicExoPlayer()
        mediaSession = MediaSession.Builder(this, player).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, buildPlaceholderNotification())
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = (application as EcoCarApplication).musicPlayerOrNull()
        if (player == null || !player.playWhenReady) {
            stopSelf()
        }
    }

    private fun buildPlaceholderNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EcoCar")
            .setContentText("Medienwiedergabe")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSilent(true)
            .build()

    companion object {
        private const val CHANNEL_ID = "ecocar_music_playback"
        private const val NOTIFICATION_ID = 7101

        fun start(context: Context) {
            val intent = Intent(context, MusicPlayerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                @Suppress("DEPRECATION")
                context.startService(intent)
            }
        }
    }
}
