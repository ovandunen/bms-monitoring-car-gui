# bms-monitoring-car-gui

EcoCar driver GUI and shared battery UI for the BMS monitoring stack.

## Modules

| Module | Role |
|--------|------|
| `:eco-car-battery-ui` | Shared Compose UI — `BatteryOverviewScreen` / `BatteryOverviewUiModel` |
| `:bms-monitoring-ipc` | AIDL client/server library (`AidlBatteryClientAdapter`, `BatterySnapshot`, `ConnectionStatus`) |
| `:composeApp` | KMP library (`androidTarget` + JVM desktop), namespace `com.fleet.ecocar` |
| `:androidApp` | Android application (`applicationId` `com.fleet.ecocar`), Hilt, depends on `:composeApp` |

## IPC setup

Publish the IPC library to Maven local (once per machine or after IPC changes):

```bash
./gradlew :bms-monitoring-ipc:publishReleasePublicationToMavenLocal
```

`:composeApp` depends on `project(":bms-monitoring-ipc")` (same artifact as `com.fleet.shared:bms-monitoring-ipc:1.0.0-SNAPSHOT`).

`EcoCarApplication` creates `AidlBatteryClientAdapter` and calls `connect()` in `onCreate()`. Battery overview uses `BatteryDashboardViewModel` + `BatteryOverviewScreen` from `:eco-car-battery-ui`.

Live battery telemetry requires the separate **BMS APK** (`com.fleet.bms` from `bms-monitoring-app`) on the **same Android device or emulator** as EcoCar GUI (`com.fleet.ecocar`). IPC uses signature permission `com.fleet.bms.permission.BIND_MONITOR_SERVICE` — both apps must be signed with the **same certificate**.

## Environments: development, test, and production

### Development (local machine + device/emulator)

**Goal:** Fast iteration on UI and IPC client; optional live BMS data.

| Topic | Recommendation |
|--------|----------------|
| **Where apps run** | Physical tablet/head unit or Android emulator — IPC happens on the device, not on your Mac/PC. |
| **Signing** | Use the **same debug keystore** for both APKs. With default Gradle signing, builds on **one machine** already share `~/.android/debug.keystore`. Different laptops → different debug certs → bind fails unless you share a keystore file. |
| **Install** | Build and install both apps on the target device: BMS (`bms-monitoring-app` → `:app:installDebug`), then EcoCar (`:androidApp:installDebug`). |
| **Start order** | Either order is supported; the IPC client binds to `com.fleet.bms.action.MONITOR_SERVICE`, can start the foreground service, and retries with backoff. Use **Wake BMS** in the battery UI only if the screen stays offline. |
| **IPC library** | After changing `:bms-monitoring-ipc`, publish to Maven local or rely on `project(":bms-monitoring-ipc")` in this repo. |
| **Desktop** | `:composeApp:run` exercises UI without BMS IPC (battery uses demo data). |

```bash
# From bms-monitoring-app
./gradlew :app:installDebug

# From bms-monitoring-car-gui
./gradlew :bms-monitoring-ipc:publishReleasePublicationToMavenLocal   # if using Maven coord elsewhere
./gradlew :androidApp:installDebug
```

Verify matching signatures if bind fails:

```bash
adb shell dumpsys package com.fleet.bms | grep -A2 "signatures"
adb shell dumpsys package com.fleet.ecocar | grep -A2 "signatures"
```

### Test (CI / GitHub Actions, QA emulators)

**Goal:** Catch regressions in build, unit logic, and optionally two-app IPC — without requiring vehicle hardware.

| Topic | Recommendation |
|--------|----------------|
| **Every PR (this repo)** | `assembleDebug`, unit tests, lint — no emulator required. Publish `:bms-monitoring-ipc` when it changes. |
| **Unit / module tests** | Test `bms-monitoring-ipc`, mappers, and ViewModels with fakes; no second APK needed. |
| **Single-app instrumented tests** | EcoCar-only or BMS-only on an emulator — UI and service lifecycle, not full IPC. |
| **Two-app IPC on CI** | Use **one workflow job**, **one emulator**, and **one shared keystore** for both APKs. Do not build BMS in one job and EcoCar in another with default debug keys — GitHub creates a **new debug cert per job** unless you configure a shared keystore. |
| **Suggested split** | Fast checks on every PR; heavier “install BMS + EcoCar + smoke bind” on `main`, nightly, or `workflow_dispatch`. |
| **Checkout** | Integration workflow should check out **both** `bms-monitoring-app` and `bms-monitoring-car-gui` (or use a composite repo). |
| **Hardware** | USB/CAN/ESP32 paths stay out of GitHub — manual QA or a device lab. |

Example CI signing approach (conceptual): decode or generate one `ci-debug.keystore` per job and pass the same `signingConfig` to both Gradle builds before `adb install` of both APKs.

### Production (field devices / release)

**Goal:** Signed fleet builds on head units with reliable BMS ↔ EcoCar IPC.

| Topic | Recommendation |
|--------|----------------|
| **Signing** | Ship **both** `com.fleet.bms` and `com.fleet.ecocar` with the **same platform release key** (e.g. fleet/head-unit keystore). Signature permission is enforced at runtime. |
| **Distribution** | Install/update both APKs (or system image) per release process; document version pairs that are known compatible. |
| **IPC artifact** | Pin `com.fleet.shared:bms-monitoring-ipc` to a **release version** (not `-SNAPSHOT`) aligned with the BMS app release. |
| **Start order** | Same as development — order-agnostic bind + reconnect; no manual “install BMS first” rule for operators unless your OTA process requires it. |
| **Monitoring** | Use on-device logs (`AidlBatteryClient` tag) for bind denials (wrong signature, BMS not installed, service killed). |

### What to test where (summary)

| Environment | Build | Unit tests | EcoCar UI | BMS ↔ EcoCar IPC | CAN / USB hardware |
|-------------|-------|------------|-----------|------------------|---------------------|
| **Development** | ✓ | ✓ | ✓ device/emulator | ✓ same debug key | ✓ manual |
| **Test (CI)** | ✓ | ✓ | optional emulator | ✓ optional, shared CI key + both APKs | ✗ |
| **Test (CI)** | ✓ | ✓ | optional emulator | ✓ optional, shared CI key + both APKs | navigation location engine (optional emulator) |
| **Production** | release | as needed | field | ✓ same release key | field only |

## Java 21

If Gradle fails with `IllegalArgumentException: 25.0.2`, install JDK 21 and uncomment in `gradle.properties`:

```properties
org.gradle.java.home=/path/from/usr/libexec/java_home -v 21
```

Modules use `jvmToolchain(21)`.

## Build & run

```bash
./gradlew :androidApp:assembleDebug
./gradlew :composeApp:run
```

## Publish battery UI

```bash
./gradlew :eco-car-battery-ui:publishReleasePublicationToMavenLocal
```

Artifact: `com.fleet.shared:eco-car-battery-ui:1.0.0`

## Recent features

### Battery overview (live IPC)

- **`:eco-car-battery-ui`** — shared `BatteryOverviewScreen` fed by `BatteryDashboardViewModel` + `AidlBatteryClientAdapter`
- **Automation descriptors** — metric cards expose uiautomator `contentDescription` values such as `battery-soc=12.0`, `battery-voltage=310.0` (used by `make verify-ui-metrics` in `bms-monitoring-app`)
- **Low SOC styling** — orange SOC progress/text below 20%; **Low battery dialog** when HV SOC drops under 20% (copy is HV-only, no 12 V Bordnetz wording)

### Map & charging stations

- **MapLibre map** with station pins (`MapViewWithStationPins`) and a scrollable **Ladestationen** list
- **GPS fallback** — when location is unavailable, requests use Berlin demo coordinates (aligned with BMS CSMS defaults) so offline/dev runs still show cached stations
- **IPC preload** — on BMS bind, EcoCar publishes Room offline cache to IPC when CSMS is down or SOC is low

### Offline turn-by-turn navigation (Graph depot sync)

- **On-device routing** — GraphHopper 10.2 (`graphhopper-core`) computes routes locally against a bundled/downloaded `.gh` graph (Senegal & Gambia). No server-side routing calls.
- **Storage budget** — Strict ~1.2 GB on-device budget. `NavigationStorageBudget` enforces the cap; `VehicleNavigationCoordinator` checks `filesDir.freeSpace` before routing.
- **Depot sync (Wi-Fi only)** — `GraphDepotSyncPolicy` gates downloads to unmetered Wi-Fi. Cellular downloads are intentionally blocked.
- **Critical implementation constraints for `GraphDepotSyncTrigger`:**
  1. **Stream unzip to disk:** Do *not* buffer the 400MB+ zip into RAM. Pipe `ZipInputStream` directly to `FileOutputStream`.
  2. **Atomic download:** Download to a `.tmp` file. Only rename to `.zip` (atomic on ext4) after verifying the file size against the manifest. Protects against corrupted files from flaky depot Wi-Fi.
  3. **Delete the zip:** Immediately delete the `.zip` file after successful extraction. Leaving it permanently bleeds ~400MB of storage.
- **Testing the download cycle:** Use OkHttp `MockWebServer` in `testDebugUnitTest` to serve a tiny, valid zip (e.g., 10KB containing a 3-node synthetic graph). Verify the temp file is created, extracted to the correct directory, and the zip is deleted. Do not depend on the CSMS build script for Android unit tests.

### Navigation location engine (instrumented integration test)

Turn-by-turn maneuver updates are validated with a **device/emulator instrumented test** that fakes GPS inside the app process and mocks routing — no GraphHopper, no `adb shell geo fix`, and **no changes to `main` / `release` sources**.

| Piece | Location |
|-------|----------|
| `TestLocationEngine` | `androidApp/src/sharedTest/.../navigation/TestLocationEngine.kt` |
| Hilt test DI (`FakeRouteProvider`, pre-baked Directions JSON) | `androidApp/src/sharedTest/.../di/TestNavigationModule.kt` |
| UI test + test host activity | `composeApp/src/androidInstrumentedTest/.../navigation/NavigationUiTest.kt` |

**What it does**

1. `FakeRouteProvider` returns pre-baked Mapbox Directions v5 JSON (`Head north` → `Turn right`) — never calls GraphHopper.
2. `NavigationTestHostActivity` starts MapLibre `NavigationView` with `TestLocationEngine` and off-route detection disabled (manual wiring — KMP androidTest skips Hilt KSP).
3. The test calls `testLocationEngine.simulateLocation(lat, lng, bearing)` and asserts maneuver text via Compose (`onNodeWithText`).

**Run (requires connected emulator or device; map tiles need network)**

On macOS, if `adb: command not found`, the Android SDK is usually at `~/Library/Android/sdk` (see `sdk.dir` in `local.properties`). Add platform-tools and emulator to your shell **once**:

```bash
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
```

Then:

```bash
# 1. Start an emulator (full boot can take 1–2 minutes with -no-snapshot-load)
emulator -avd Pixel_Tablet -no-snapshot-load &

# Wait until adb reports "device" (NOT "offline") and boot completes
adb wait-for-device
while [ "$(adb get-state 2>/dev/null)" != "device" ]; do
  echo "waiting for emulator to come online..."
  sleep 3
done
adb shell 'while [ -z "$(getprop sys.boot_completed 2>/dev/null)" ]; do sleep 2; done'

adb devices -l
# Must show: emulator-5554   device ...   (never "offline" or empty)

# 2. Build test APK (works without a device)
./gradlew :composeApp:assembleDebugAndroidTest

# 3. Run only the navigation harness
./gradlew :composeApp:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.fleet.ecocar.navigation.NavigationUiTest
```

One-off without changing PATH (SDK path from this repo’s `local.properties`):

```bash
~/Library/Android/sdk/platform-tools/adb devices -l
~/Library/Android/sdk/emulator/emulator -list-avds
```

**Tips**

| Situation | What to do |
|-----------|------------|
| `adb: command not found` (macOS) | `export PATH="$HOME/Library/Android/sdk/platform-tools:$HOME/Library/Android/sdk/emulator:$PATH"` or use full paths under `~/Library/Android/sdk/` |
| `not enough space` / `install-create` fails | androidTest APK is large (~300–700MB with all ABIs). Free emulator storage (`adb shell df -h /data`), wipe AVD data, or create an AVD with ≥8GB internal; rebuild after `:composeApp` ABI filters (`arm64-v8a`, `x86_64` only) |
| `No connected devices!` | Emulator not running, or still `offline` — wait for `adb get-state` → `device` before Gradle (see boot loop above) |
| `adb: device offline` during boot | Normal for ~30s after start; re-run the `while [ "$(adb get-state)" != "device" ]` loop — do not run Gradle until `adb devices -l` shows `device` |
| `MapLibreConfigurationException` / `MapView` inflate fails | Test host must call `MapLibre.getInstance` before `NavigationView`; ensure `maptiler.api_key` is in `local.properties` |
| `ClassCastException: FragmentActivity` | Test host must extend `AppCompatActivity` (MapLibre `NavigationView` requires `FragmentActivity`) |
| Test times out on maneuver text | Ensure emulator has network (MapLibre map load); retry with a cold emulator |
| `ClassNotFoundException: Hilt_*` | Instrumented tests use manual wiring (no Hilt on KMP `:composeApp` androidTest); rebuild with `./gradlew :composeApp:assembleDebugAndroidTest` |
| Production navigation unchanged | Harness is isolated in `sharedTest` + `androidInstrumentedTest`; `VehicleNavigationCoordinator` still uses fused GPS at runtime |

**JVM-only routing tests** (no location engine, no UI):

```bash
./gradlew :composeApp:testDebugUnitTest --tests "com.fleet.ecocar.infrastructure.navigation.*"
```

### IPC client

- `BmsTelemetryBinder` starts the BMS monitor service and queues map refresh until bind completes
- `ObserveVcuBatteryAlerts` triggers Stufe 2 (≤ 20 % SOC) or Stufe 3 (≤ 5 % SOC) dialogs once per episode (same 20 % threshold as BMS Ladestation preload)

## Unit tests (no emulator)

```bash
# Battery metric descriptors (integration-test contract values)
./gradlew :eco-car-battery-ui:test

# Snapshot → UI model → descriptor strings
./gradlew :composeApp:testDebugUnitTest --tests "com.fleet.ecocar.ui.battery.BatteryOverviewViewModelTest"

# Map / charging-station logic
./gradlew :composeApp:desktopTest --tests "com.fleet.ecocar.map.*"
```

## Integration testing (Makefile, sibling repo)

Full CAN → BMS → EcoCar → UI pipeline is driven from **`bms-monitoring-app/Makefile`** (not this repo). Run from the BMS app directory:

```bash
cd ../bms-monitoring-app
make integration-test-ui
```

That target runs `build-install` (both APKs), sends test CAN frames (SOC **12%**), verifies IPC in logcat, then **`verify-ui-metrics`** on the emulator.

**Tips**

| Situation | What to do |
|-----------|------------|
| `battery-soc=12.0 not in UI dump` | Run `make build-install` in `bms-monitoring-app` so the emulator gets the latest EcoCar APK with automation descriptors |
| Low-battery dialog blocks the test | Expected at 12% SOC — Makefile dismisses it; ensure EcoCar opens on **Battery** (default start destination) |
| `relay port 9999 not open` | First Lima run is slow (`apt-get` in Docker); retry `make relay` or wait up to 180 s |
| IPC bind fails | Same debug keystore on both APKs — see [Development](#development-local-machine--deviceemulator) |
| Stale emulator / ANR | `make shutdown` then rerun `make integration-test-ui` |

See [`bms-monitoring-app/README.md`](../bms-monitoring-app/README.md#integration-testing-makefile) for relay, clean/shutdown, and CSMS targets.
