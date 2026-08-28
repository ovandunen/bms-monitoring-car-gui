package com.fleet.ecocar

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.media3.exoplayer.ExoPlayer
import com.bms.monitor.aidl.VehicleLocationSnapshot
import com.fleet.ecocar.composeapp.BuildConfig
import com.fleet.ecocar.ipc.BmsTelemetryBinder
import com.fleet.ecocar.map.ChargingStationMapRequestPolicy
import com.fleet.ecocar.map.EcoChargingStation
import com.fleet.ecocar.music.MusicPlaybackSurface
import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import com.fleet.ecocar.telemetry.toEcoBmsTelemetry
import com.fleet.ecocar.ui.top.TopBarMusicState
import com.fleet.shared.bms.ipc.infrastructure.AidlBatteryClientAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication
import com.fleet.ecocar.map.ChargingStationMapState

open class EcoCarApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Application-scoped driving adapter for BMS monitor IPC ([AidlBatteryClientAdapter]).
     * Survives rotation; connect once in [onCreate], disconnect only on process death.
     */
    lateinit var batteryClient: AidlBatteryClientAdapter
        private set

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

    val topBarMusicState: StateFlow<TopBarMusicState> = _topBarMusic.asStateFlow()

    private val _ecoBmsTelemetry = MutableStateFlow<EcoBmsTelemetry?>(null)

    val ecoBmsTelemetry: StateFlow<EcoBmsTelemetry?> = _ecoBmsTelemetry.asStateFlow()

    private val _chargingStations = MutableStateFlow<List<EcoChargingStation>>(emptyList())

    // NEW: Vehicle location StateFlow
    private val _vehicleLocation = MutableStateFlow<VehicleLocationSnapshot?>(null)
    val vehicleLocation: StateFlow<VehicleLocationSnapshot?> = _vehicleLocation.asStateFlow()

    val chargingStations: StateFlow<List<EcoChargingStation>> = _chargingStations.asStateFlow()

    private val _chargingStationsRefreshing = MutableStateFlow(false)
    val chargingStationsRefreshing: StateFlow<Boolean> = _chargingStationsRefreshing.asStateFlow()

    private var chargingStationsRefreshJob: Job? = null

    private var bmsTelemetryBinder: BmsTelemetryBinder? = null

    @Volatile
    var musicPlaybackSurface: MusicPlaybackSurface? = null


    companion object {
        const val BROWSER_DEFAULT_HOME_URL: String = "https://www.startpage.com"

        /** Matches BMS GUI refresh timeout + margin when CSMS is unavailable. */
        private const val CHARGING_STATIONS_REFRESH_MAX_MS = 12_000L

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
        MapLibre.getInstance(
            this,
            BuildConfig.MAPTILER_API_KEY,
            WellKnownTileServer.MapTiler,
        )
        super.onCreate()
        instance = this
        mainHandler.post(clockRunnable)

        batteryClient = AidlBatteryClientAdapter(this, appScope)
        batteryClient.connect()

        appScope.launch {
            batteryClient.batteryState.collect { snap ->
                if (snap == null || snap.timestamp == 0L) return@collect
                val ipc = snap.toEcoBmsTelemetry()
                _ecoBmsTelemetry.value = _ecoBmsTelemetry.value?.let { existing ->
                    ipc.copy(
                        cellVolts = ipc.cellVolts.ifEmpty { existing.cellVolts },
                        packHumidity = if (ipc.packHumidity == 0f) existing.packHumidity else ipc.packHumidity,
                        pm25 = if (ipc.pm25 == 0) existing.pm25 else ipc.pm25,
                        pm10 = if (ipc.pm10 == 0) existing.pm10 else ipc.pm10,
                    )
                } ?: ipc
            }
        }

        bmsTelemetryBinder = BmsTelemetryBinder(
            this,
            onTelemetry = { legacy ->
                val ipcLive = batteryClient.batteryState.value?.timestamp?.let { it > 0L } == true
                if (!ipcLive) {
                    _ecoBmsTelemetry.value = legacy
                } else {
                    _ecoBmsTelemetry.value = _ecoBmsTelemetry.value?.let { current ->
                        current.copy(
                            cellVolts = legacy.cellVolts.ifEmpty { current.cellVolts },
                            packHumidity = if (current.packHumidity == 0f) legacy.packHumidity else current.packHumidity,
                            pm25 = if (current.pm25 == 0) legacy.pm25 else current.pm25,
                            pm10 = if (current.pm10 == 0) legacy.pm10 else current.pm10,
                        )
                    } ?: legacy
                }
            },
            onChargingStations = { stations ->
                _chargingStations.value = ChargingStationMapRequestPolicy.applyIpcUpdate(stations)
                _chargingStationsRefreshing.value = false
            },
            // NEW: onLocationUpdate callback
            // NOTE: BmsTelemetryBinder.kt must also be updated to accept this parameter
            onLocationUpdate = { location ->
                _vehicleLocation.value = VehicleLocationSnapshot(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed,
                timestamp = System.currentTimeMillis(),
                accuracy = location.accuracy
            )
            }
        ).also { it.connect() }
    }

    // -------------------------------------------------------------------------
    // TODO: Restore the real implementations below from git history.
    // These were deleted/replaced with placeholders by an AI coding assistant.
    // -------------------------------------------------------------------------

    private fun formatClock(): String {
        val now = java.util.Calendar.getInstance()
        val hour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = now.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    private fun formatClockStatic(): String = formatClock()


    private fun publishTopBarFromPlayer() {
        val player = _exoPlayer ?: return
        _topBarMusic.value = _topBarMusic.value.copy(
            isPlaying = player.isPlaying,
            currentPosition = player.currentPosition,
            duration = player.duration.coerceAtLeast(0L).toString(),
        )
    }

    // -------------------------------------------------------------------------
// Music player helpers — called by MusicPlayerService
// -------------------------------------------------------------------------

    fun ensureMusicExoPlayer(): ExoPlayer {
        synchronized(musicLock) {
            _exoPlayer?.let { return it }
            val player = ExoPlayer.Builder(this).build()
            _exoPlayer = player
            return player
        }
    }

    fun requestChargingStationsForMap(radiusMeters: Double = 0.0) {
        _chargingStationsRefreshing.value = true

        val loc = _vehicleLocation.value
        if (loc != null) {
            bmsTelemetryBinder?.requestChargingStationsForDisplay(
                latitude = loc.latitude,
                longitude = loc.longitude,
                radiusMeters = radiusMeters,
            )
        } else {
            // No location yet — fall back to whatever's cached so the UI isn't empty.
            bmsTelemetryBinder?.publishCachedChargingStations()
        }

        chargingStationsRefreshJob?.cancel()
        chargingStationsRefreshJob = appScope.launch {
            kotlinx.coroutines.delay(CHARGING_STATIONS_REFRESH_MAX_MS)
            _chargingStationsRefreshing.value = false
        }
    }

    fun musicPlayerOrNull(): ExoPlayer? = _exoPlayer


}