package com.fleet.ecocar.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.fleet.ecocar.composeapp.BuildConfig
import com.fleet.ecocar.di.FakeRouteProvider
import com.fleet.ecocar.di.NavigationTestFixtures
import org.junit.Rule
import org.junit.Test
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.navigation.android.navigation.ui.v5.NavigationView
import org.maplibre.navigation.android.navigation.ui.v5.OnNavigationReadyCallback
import org.maplibre.navigation.android.navigation.ui.v5.listeners.NavigationListener
import org.maplibre.navigation.core.models.DirectionsResponse
import org.maplibre.navigation.core.navigation.MapLibreNavigation
import org.maplibre.navigation.core.navigation.MapLibreNavigationOptions
import org.maplibre.navigation.core.routeprogress.ProgressChangeListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Turn-by-turn UI harness: mocked Mapbox v5 route JSON + programmatic GPS via [TestLocationEngine].
 *
 * Hilt is intentionally **not** used here — KSP is skipped for `:composeApp` androidTest on KMP,
 * so dependencies are wired manually in [NavigationTestHostActivity].
 */
class NavigationUiTest {

    @get:Rule
    val composeTestRule = createNavigationHostComposeRule()

    @Test
    fun turnByTurn_maneuverBanner_updatesWithSimulatedGps() {
        val activity = composeTestRule.activity
        val testLocationEngine = activity.testLocationEngine

        require(activity.awaitNavigationStarted(timeoutSeconds = 30)) {
            "MapLibre navigation did not start within 30s"
        }
        require(activity.awaitFirstProgress(timeoutSeconds = 15)) {
            "MapLibre navigation did not emit route progress within 15s"
        }
        composeTestRule.waitForIdle()
        testLocationEngine.simulateLocation(
            NavigationTestFixtures.LAT_START,
            NavigationTestFixtures.LNG_START,
            bearing = 0f,
        )
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeTestRule.onNodeWithText("Head north").assertExists()
            }.isSuccess
        }
        composeTestRule.onNodeWithText("Head north").assertIsDisplayed()

        testLocationEngine.simulateLocation(
            NavigationTestFixtures.LAT_TURN,
            NavigationTestFixtures.LNG_TURN,
            bearing = 90f,
        )
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            runCatching {
                composeTestRule.onNodeWithText("Turn right").assertExists()
            }.isSuccess
        }
        composeTestRule.onNodeWithText("Turn right").assertIsDisplayed()
        composeTestRule.onNodeWithTag(NavigationTestHostActivity.INSTRUCTION_TEST_TAG)
            .assertIsDisplayed()
    }
}

/**
 * Instrumented-test host for MapLibre [NavigationView] with programmatic [TestLocationEngine].
 * Instruction text is mirrored in Compose for [onNodeWithText] assertions.
 */
class NavigationTestHostActivity :
    AppCompatActivity(),
    OnNavigationReadyCallback,
    NavigationListener {

    lateinit var testLocationEngine: TestLocationEngine
        private set

    private lateinit var routeProvider: FakeRouteProvider
    private var navigationView: NavigationView? = null
    private var mapLibreNavigation: MapLibreNavigation? = null
    private val navigationStartedLatch = CountDownLatch(1)
    private val firstProgressLatch = CountDownLatch(1)
    private var instructionText by mutableStateOf("")

    fun awaitNavigationStarted(timeoutSeconds: Long): Boolean =
        navigationStartedLatch.await(timeoutSeconds, TimeUnit.SECONDS)

    fun awaitFirstProgress(timeoutSeconds: Long): Boolean =
        firstProgressLatch.await(timeoutSeconds, TimeUnit.SECONDS)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_NoActionBar)
        MapLibre.getInstance(
            applicationContext,
            BuildConfig.MAPTILER_API_KEY,
            WellKnownTileServer.MapTiler,
        )
        super.onCreate(savedInstanceState)

        testLocationEngine = TestLocationEngine()
        routeProvider = FakeRouteProvider().apply {
            directionsJson = intent.getStringExtra(EXTRA_DIRECTIONS_JSON)
                ?: NavigationTestFixtures.MOCK_DIRECTIONS_JSON
        }

        val navView = NavigationView(this)
        navigationView = navView
        navView.onCreate(savedInstanceState)
        navView.initialize(this)

        setContent {
            Column(Modifier.fillMaxSize()) {
                Text(
                    text = instructionText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(INSTRUCTION_TEST_TAG),
                )
                AndroidView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    factory = { navView },
                )
            }
        }

        // Start the navigation engine immediately — do not wait for map style/tiles.
        // NavigationView only fires onNavigationReady after ThemeSwitcher loads map tiles,
        // which is flaky in CI/emulator tests without guaranteed network.
        startNavigationEngine()
    }

    override fun onNavigationReady(isRunning: Boolean) = Unit

    private fun startNavigationEngine() {
        val json = routeProvider.directionsJson
        val route = DirectionsResponse.fromJson(json).routes.first()

        val navigationOptions = MapLibreNavigationOptions.Builder()
            .withEnableOffRouteDetection(false)
            .withSnapToRoute(true)
            .withDefaultMilestonesEnabled(false)
            .build()

        val progressListener = ProgressChangeListener { _, routeProgress ->
            val instruction = routeProgress.currentLegProgress
                .currentStep
                .maneuver
                .instruction
                .orEmpty()
            if (instruction.isNotEmpty()) {
                runOnUiThread { instructionText = instruction }
                firstProgressLatch.countDown()
            }
        }

        MapLibreNavigation(navigationOptions, testLocationEngine).also {
            mapLibreNavigation = it
            it.addProgressChangeListener(progressListener)
            it.startNavigation(route)
        }
        navigationStartedLatch.countDown()
    }

    override fun onStart() {
        super.onStart()
        navigationView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        navigationView?.onResume()
    }

    override fun onPause() {
        navigationView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        navigationView?.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        navigationView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navigationView?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mapLibreNavigation?.onDestroy()
        mapLibreNavigation = null
        navigationView?.onDestroy()
        navigationView = null
        super.onDestroy()
    }

    override fun onCancelNavigation() {
        finish()
    }

    override fun onNavigationFinished() {
        finish()
    }

    override fun onNavigationRunning() = Unit

    companion object {
        const val EXTRA_DIRECTIONS_JSON = "extra_directions_json"
        const val INSTRUCTION_TEST_TAG = "nav-instruction"
    }
}

private fun createNavigationHostComposeRule(): AndroidComposeTestRule<
    ActivityScenarioRule<NavigationTestHostActivity>,
    NavigationTestHostActivity,
    > {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = Intent(context, NavigationTestHostActivity::class.java).putExtra(
        NavigationTestHostActivity.EXTRA_DIRECTIONS_JSON,
        NavigationTestFixtures.MOCK_DIRECTIONS_JSON,
    )
    return AndroidComposeTestRule(
        activityRule = ActivityScenarioRule(intent),
        activityProvider = { rule ->
            lateinit var activity: NavigationTestHostActivity
            rule.scenario.onActivity { activity = it }
            activity
        },
    )
}
