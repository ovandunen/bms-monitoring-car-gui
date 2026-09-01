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
     *
     * TECHNICAL DEBT (see TECHNICAL_DEBT.md #1): this is the Family A
     * client (com.fleet.shared.bms.ipc). It targets a service class name
     * ("ch.ecocar.bms.BmsMonitoringService") that does not exist in the
     * manifest, and even if the name were fixed, IBmsService.Stub.asInterface
     * would bind against a Family B binder using an incompatible AIDL
     * descriptor - the two contracts were never designed to talk to each
     * other. Kept instantiated and connected (harmless: it just retries
     * with backoff and never succeeds) so nothing else referencing
     * `batteryClient` breaks, but its state is deliberately NOT consumed
     * anywhere below anymore. The working path is BmsTelemetryBinder
     * (Family B) via bmsTelemetryBinder, further down this file.
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

    // Vehicle location StateFlow - fed by BmsTelemetryBinder's onLocationUpdate
    // callback below, which was previously never invoked. See
    // BmsTelemetryBinder.kt for the fix.
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

        // REMOVED (see TECHNICAL_DEBT.md #1): this block previously
        // collected batteryClient.batteryState - a field that does not
        // exist on AidlBatteryClientAdapter (it exposes batterySnapshot,
        // of an incompatible domain type), and merged it into
        // _ecoBmsTelemetry as the presumed-primary source. Family A
        // (com.fleet.shared.bms.ipc) is a separate, incompatible AIDL
        // contract from the one actually served by BmsMonitorService
        // (Family B, com.bms.monitor.aidl) - it cannot successfully bind,
        // so this collector could never have emitted real data. Deleting
        // it rather than patching the field name, since patching it would
        // still leave a broken/never-firing path pretending to be primary.
        // BmsTelemetryBinder (below) is the confirmed working path.

        bmsTelemetryBinder = BmsTelemetryBinder(
            this,
            onTelemetry = { legacy ->
                // BmsTelemetryBinder (Family B) is the sole confirmed
                // working telemetry source - see TECHNICAL_DEBT.md #1 for
                // why the previous batteryClient-primary merge logic was
                // removed rather than fixed in place.
                _ecoBmsTelemetry.value = legacy
            },
            onChargingStations = { stations ->
                _chargingStations.value = ChargingStationMapRequestPolicy.applyIpcUpdate(stations)
                _chargingStationsRefreshing.value = false
            },
            // Now actually invoked - see the fix in BmsTelemetryBinder.onDataUpdate.
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