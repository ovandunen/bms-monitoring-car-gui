package com.fleet.ecocar

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.fleet.ecocar.ipc.BmsTelemetryBinder
import com.fleet.ecocar.map.EcoChargingStation
import com.fleet.ecocar.music.MusicPlaybackSurface
import com.fleet.ecocar.music.RadioStation
import com.fleet.ecocar.music.Track
import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import com.fleet.ecocar.ui.top.TopBarMusicState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mozilla.geckoview.GeckoRuntime
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.mozilla.geckoview.GeckoSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class EcoCarApplication : Application() {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val clockRunnable = object : Runnable {
        override fun run() {
            val clock = formatClock()
            _topBarMusic.value = _topBarMusic.value.copy(clock = clock)
            mainHandler.postDelayed(this, 1000L)
        }
    }

    private val positionRunnable = object : Runnable {
        override fun run() {
            publishTopBarFromPlayer()
            val p = _exoPlayer
            if (p != null && p.isPlaying) {
                mainHandler.postDelayed(this, 500L)
            }
        }
    }

    private val musicLock = Any()

    @Volatile
    private var _exoPlayer: ExoPlayer? = null

    private val _topBarMusic = MutableStateFlow(TopBarMusicState(clock = formatClockStatic()))

    /** Live title / duration / source for the top bar (USB or Radio). */
    val topBarMusicState: StateFlow<TopBarMusicState> = _topBarMusic.asStateFlow()

    private val _ecoBmsTelemetry = MutableStateFlow<EcoBmsTelemetry?>(null)

    /** Letzter Snapshot vom BMS (`[BmsService]` / AIDL), sofern IPC aktiv. */
    val ecoBmsTelemetry: StateFlow<EcoBmsTelemetry?> = _ecoBmsTelemetry.asStateFlow()

    /** CSMS charging stations via BMS AIDL (live or offline cache). */
    private val _chargingStations = MutableStateFlow<List<EcoChargingStation>>(emptyList())
    val chargingStations: StateFlow<List<EcoChargingStation>> = _chargingStations.asStateFlow()

    private var bmsTelemetryBinder: BmsTelemetryBinder? = null

    @Volatile
    var musicPlaybackSurface: MusicPlaybackSurface? = null
        private set

    companion object {
        const val BROWSER_DEFAULT_HOME_URL: String = "https://www.startpage.com"

        @Volatile
        private var geckoRuntime: GeckoRuntime? = null

        @Volatile
        private var browserSession: GeckoSession? = null

        @Volatile
        private var initialBrowserNavigationIssued: Boolean = false

        private var instance: EcoCarApplication? = null

        fun geckoRuntime(): GeckoRuntime {
            geckoRuntime?.let { return it }
            synchronized(this) {
                geckoRuntime?.let { return it }
                val app = instance ?: error("EcoCarApplication not created")
                val runtime = GeckoRuntime.create(app)
                geckoRuntime = runtime
                return runtime
            }
        }

        fun browserSession(): GeckoSession {
            browserSession?.let { return it }
            synchronized(this) {
                browserSession?.let { return it }
                val session = GeckoSession().apply {
                    contentDelegate = object : GeckoSession.ContentDelegate {}
                }
                browserSession = session
                return session
            }
        }

        fun scheduleInitialBrowserLoadIfNeeded(session: GeckoSession) {
            if (initialBrowserNavigationIssued) return
            synchronized(this) {
                if (initialBrowserNavigationIssued) return
                initialBrowserNavigationIssued = true
                session.loadUri(BROWSER_DEFAULT_HOME_URL)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        mainHandler.post(clockRunnable)
        bmsTelemetryBinder = BmsTelemetryBinder(
            this,
            onTelemetry = { _ecoBmsTelemetry.value = it },
            onChargingStations = { _chargingStations.value = it },
        ).also { it.connect() }
    }

    /** Requests nearby stations from BMS → CSMS MQTT; uses device GPS when available. */
    fun refreshChargingStationsNearby(radiusMeters: Double = 0.0) {
        val fused = LocationServices.getFusedLocationProviderClient(this)
        val cancel = CancellationTokenSource()
        fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancel.token)
            .addOnSuccessListener { loc ->
                val lat = loc?.latitude ?: 52.52
                val lon = loc?.longitude ?: 13.405
                bmsTelemetryBinder?.refreshChargingStations(lat, lon, radiusMeters)
            }
            .addOnFailureListener {
                bmsTelemetryBinder?.refreshChargingStations(52.52, 13.405, radiusMeters)
            }
    }

    override fun onTerminate() {
        bmsTelemetryBinder?.disconnect()
        bmsTelemetryBinder = null
        super.onTerminate()
    }

    private fun formatClock(): String = formatClockStatic()

    fun ensureMusicExoPlayer(): ExoPlayer {
        synchronized(musicLock) {
            if (_exoPlayer == null) {
                val audioAttrs = AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build()
                _exoPlayer = ExoPlayer.Builder(this)
                    .setAudioAttributes(audioAttrs, true)
                    .build()
                _exoPlayer!!.addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            publishTopBarFromPlayer()
                            when (playbackState) {
                                Player.STATE_READY, Player.STATE_BUFFERING -> schedulePositionTicks()
                                else -> mainHandler.removeCallbacks(positionRunnable)
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            publishTopBarFromPlayer()
                            if (isPlaying) {
                                schedulePositionTicks()
                            } else {
                                mainHandler.removeCallbacks(positionRunnable)
                            }
                        }

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            publishTopBarFromPlayer()
                        }

                        override fun onPositionDiscontinuity(
                            oldPosition: Player.PositionInfo,
                            newPosition: Player.PositionInfo,
                            reason: Int,
                        ) {
                            publishTopBarFromPlayer()
                        }
                    },
                )
            }
            return _exoPlayer!!
        }
    }

    private fun schedulePositionTicks() {
        mainHandler.removeCallbacks(positionRunnable)
        mainHandler.post(positionRunnable)
    }

    private fun publishTopBarFromPlayer() {
        val p = _exoPlayer ?: return
        val meta = p.currentMediaItem?.mediaMetadata
        val titleLine = buildString {
            val t = meta?.title?.toString().orEmpty()
            val a = meta?.artist?.toString().orEmpty()
            when {
                t.isNotEmpty() && a.isNotEmpty() -> append("$t – $a")
                t.isNotEmpty() -> append(t)
                a.isNotEmpty() -> append(a)
                else -> append("—")
            }
        }
        val pos = formatDurMs(p.currentPosition)
        val dur = if (p.duration > 0) formatDurMs(p.duration) else "--:--"
        val durString = "$pos / $dur"
        val source = when (musicPlaybackSurface) {
            MusicPlaybackSurface.USB -> "USB ${p.currentMediaItemIndex + 1}"
            MusicPlaybackSurface.RADIO -> "Radio"
            null -> ""
        }
        _topBarMusic.value = _topBarMusic.value.copy(
            title = titleLine,
            duration = durString,
            source = source,
        )
    }

    fun playUsbTracks(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) return
        val player = ensureMusicExoPlayer()
        musicPlaybackSurface = MusicPlaybackSurface.USB
        val items = tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.uri)
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .apply {
                            track.albumArtUri?.let { setArtworkUri(it) }
                        }
                        .build(),
                )
                .build()
        }
        val safeIndex = startIndex.coerceIn(0, items.lastIndex)
        player.setMediaItems(items, safeIndex, C.TIME_UNSET)
        player.prepare()
        player.play()
        MusicPlayerService.start(this)
        publishTopBarFromPlayer()
        schedulePositionTicks()
    }

    fun playRadioStation(station: RadioStation) {
        val player = ensureMusicExoPlayer()
        musicPlaybackSurface = MusicPlaybackSurface.RADIO
        val item = MediaItem.Builder()
            .setUri(station.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtist(station.genre.ifBlank { station.country })
                    .build(),
            )
            .build()
        player.setMediaItem(item)
        player.prepare()
        player.play()
        MusicPlayerService.start(this)
        publishTopBarFromPlayer()
        schedulePositionTicks()
    }

    fun musicPlayerOrNull(): ExoPlayer? = _exoPlayer

    private fun formatDurMs(ms: Long): String {
        if (ms <= 0L) return "0:00"
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        return "%d:%02d".format(m, s)
    }
}

private fun formatClockStatic(): String =
    SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
