# bms-monitoring-car-gui

EcoCar driver GUI and shared battery UI for the BMS monitoring stack.

## Modules

| Module | Role |
|--------|------|
| `:eco-car-battery-ui` | Shared Compose UI — `BatteryOverviewScreen` / `BatteryOverviewUiModel` |
| `bms-monitoring-ipc` (sibling, `includeBuild`) | AIDL client/server library (`AidlBatteryClientAdapter`, `BatterySnapshot`, `ConnectionStatus`) |
| `:composeApp` | KMP library (`androidTarget` + JVM desktop). Android `namespace` `com.fleet.ecocar.composeapp`; Kotlin sources `com.fleet.ecocar` |
| `:androidApp` | Android application (`applicationId` / `namespace` `com.fleet.ecocar`), Hilt, depends on `:composeApp` |

## IPC setup

The IPC library is the sibling repo `bms-monitoring-ipc`, consumed via `includeBuild("../bms-monitoring-ipc")` as `com.fleet.shared:bms-monitoring-ipc:1.2.0-SNAPSHOT` (same version as `bms-monitoring-ipc/build.gradle.kts`). `IpcContract.IPC_VERSION` is **2**.

`:composeApp` androidMain depends on those coordinates (Gradle substitutes the included build).

`EcoCarApplication` constructs `AidlBatteryClientAdapter` and calls `connect()` in `onCreate()`. Battery overview (`BatteryDashboardViewModel` + `BatteryOverviewScreen`) reads that client. The client binds package `ch.ecocarsolaire.bms` with action `ch.ecocarsolaire.bms.action.DASHBOARD_SERVICE` (`AidlBatteryClientAdapter`). EcoCar declares `uses-permission` `ch.ecocar.bms.permission.BIND_MONITOR_SERVICE` (signature on the BMS APK). Both apps must be signed with the **same certificate**.

A second binder, `BmsTelemetryBinder`, uses `ComponentName(ch.ecocarsolaire.bms, ch.ecocar.bms.BmsMonitorService)` for Family B (`com.bms.monitor.aidl`) charging-station callbacks and map location. BMS `applicationId` is `ch.ecocarsolaire.bms`.

## Environments: development, test, and production

### Development (local machine + device/emulator)

**Goal:** Fast iteration on UI and IPC client; optional live BMS data.

| Topic | Recommendation |
|--------|----------------|
| **Where apps run** | Physical tablet/head unit or Android emulator — IPC happens on the device, not on your Mac/PC. |
| **Signing** | Use the **same debug keystore** for both APKs. This repo’s `:androidApp` debug signing uses `~/ecocar-shared-debug.keystore` (alias `ecocar-debug`). Different laptops → different certs → bind fails unless you share a keystore file. |
| **Install** | Build and install both apps on the target device: BMS (`bms-monitoring-app` → `:app:installDebug`), then EcoCar (`:androidApp:installDebug`). |
| **Start order** | Either order is supported. Family A (`AidlBatteryClientAdapter`) binds `ch.ecocarsolaire.bms.action.DASHBOARD_SERVICE`, can `startForegroundService`, and retries with backoff. Use **Wake BMS** on the battery offline/error panels if the screen stays offline. |
| **IPC library** | After changing the sibling `bms-monitoring-ipc` library, rebuild this project — Gradle resolves it via `includeBuild`. |
| **Desktop** | `:composeApp:run` exercises UI without BMS IPC (battery uses demo data). |

```bash
# From bms-monitoring-app
./gradlew :app:installDebug

# From bms-monitoring-car-gui
./gradlew :androidApp:installDebug
```

Verify matching signatures if bind fails:

```bash
adb shell dumpsys package ch.ecocarsolaire.bms | grep -A2 "signatures"
adb shell dumpsys package com.fleet.ecocar | grep -A2 "signatures"
```

### Test (CI / GitHub Actions, QA emulators)

**Goal:** Catch regressions in build, unit logic, and optionally two-app IPC — without requiring vehicle hardware.

| Topic | Recommendation |
|--------|----------------|
| **Every PR (this repo)** | `assembleDebug`, unit tests, lint — no emulator required. Rebuild after IPC library changes (resolved via `includeBuild`). |
| **Unit / module tests** | Test mappers and ViewModels with fakes; library tests run in `bms-monitoring-ipc` (`cd ../bms-monitoring-ipc && ./gradlew test`). |
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
| **Signing** | Ship **both** `ch.ecocarsolaire.bms` and `com.fleet.ecocar` with the **same platform release key** (e.g. fleet/head-unit keystore). Signature permission is enforced at runtime. |
| **Distribution** | Install/update both APKs (or system image) per release process; document version pairs that are known compatible. |
| **IPC artifact** | Pin `com.fleet.shared:bms-monitoring-ipc` to a **release version** (not `-SNAPSHOT`) aligned with the BMS app release. Current library version in source is `1.2.0-SNAPSHOT`. |
| **Start order** | Same as development — order-agnostic bind + reconnect; no manual “install BMS first” rule for operators unless your OTA process requires it. |
| **Monitoring** | Use on-device logs (`AidlBatteryClient` tag) for bind denials (wrong signature, BMS not installed, service killed). |

### What to test where (summary)

| Environment | Build | Unit tests | EcoCar UI | BMS ↔ EcoCar IPC | CAN / USB hardware |
|-------------|-------|------------|-----------|------------------|---------------------|
| **Development** | ✓ | ✓ | ✓ device/emulator | ✓ same debug key | ✓ manual |
| **Test (CI)** | ✓ | ✓ | optional emulator | ✓ optional, shared CI key + both APKs | ✗ |
| **Production** | release | as needed | field | ✓ same release key | field only |

## Java 21

If Gradle fails with `IllegalArgumentException: 25.0.2`, install JDK 21 and uncomment in `gradle.properties`:

```properties
org.gradle.java.home=/path/from/usr/libexec/java_home -v 21
```

Gradle wrapper is **9.3.1**. `:androidApp`, `:composeApp`, and `:eco-car-battery-ui` use `jvmToolchain(17)` and Java 17 compile options.

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
- **Automation descriptors** — metric cards expose uiautomator `contentDescription` values such as `battery-soc=50.0` when the BMS Makefile sends the integration contract pack frame (`can.pack.data` byte 3 = `0x32` → SOC **50%**; `TEST_SOC := 50` in `bms-monitoring-app`)
- **Low SOC styling** — orange SOC progress/text when SOC is in `0.01f` until `LadestationSocPolicy.LOW_BATTERY_PERCENT` (**20f** companion constant). **Low battery dialog** (`ObserveVcuLowBattery`) uses `BuildConfig.LOW_BATTERY_PERCENT` (name `LOW_BATTERY_PERCENT`; default **20** from `low.battery.percent` / `"20"` in `composeApp/build.gradle.kts`). Dialog copy is HV-only (`dialog_low_battery_body`)

### Map & charging stations

- **MapLibre map** with station pins (`MapViewWithStationPins`) and a scrollable **Ladestationen** list
- **GPS fallback for station refresh** — when a GPS fix is missing, `ChargingStationMapRequestPolicy` uses 52.52 / 13.405 so `refreshChargingStations` can still be called
- **IPC preload** — on Family B bind, EcoCar calls `getCachedChargingStations()` (`BmsTelemetryBinder`)

Charging-station **live CSMS data is outside Milestone 2**: BMS `CsmsMqttClient` / `ChargingStationCoordinator` methods that would fill stations are stubs (`TODO` / no-op connect). EcoCar map/list/AIDL client code above still exists.

### IPC client

- `BmsTelemetryBinder` starts `BmsMonitorService` via explicit `ComponentName` and queues map refresh until bind completes
- `ObserveVcuLowBattery` triggers the low-battery dialog once per low-SOC episode using `BuildConfig.LOW_BATTERY_PERCENT`

### Milestone 2 driver UI

Battery **Trips** tab lists sessions from `AidlBatteryClientAdapter.getTripSessions` (`BatteryTripsContent`). Trip **reset** is a long-press on the bottom-bar trip chip (`resetTrip()` AIDL), not a button on the trip list. Vehicle status tile uses IPC `VehicleStatus` (`Driving` if the BMS snapshot was computed with speed &gt; 1 km/h; otherwise `Standby` (`Charging` is reserved in VehicleStatus and not produced by the BMS app yet)). Stale pack data shows the **No battery data** hint; cloud flag shows Online/Offline. Family A `IPC_VERSION` mismatch sets `ConnectionStatus.Error(IPC_VERSION_MISMATCH)` and the dashboard error panel uses `battery_ipc_version_mismatch`. String resources: `composeResources/values`, `values-de`, `values-en`, `values-wo`.

### Location on map

Map vehicle position is `EcoCarApplication.vehicleLocation`, filled from Family B AIDL (`BmsTelemetryBinder.onLocationChanged` / stamped `BmsData`). Known limitation (SEEN 2026-09-30): the Pixel_Tablet emulator delivers no GPS fixes, so the location part of `make acceptance-test` fails on the emulator; location acceptance happens on the tablet with the G-Mouse USB GPS.

## Unit tests (no emulator)

```bash
# Battery metric descriptors (integration-test contract values)
./gradlew :eco-car-battery-ui:test

# Snapshot → UI model → descriptor strings
./gradlew :composeApp:testDebugUnitTest --tests "com.fleet.ecocar.ui.battery.BatteryOverviewViewModelTest"

# Map / charging-station logic (commonTest: EcoMapStationPresenterTest, ChargingStationMapLoadUseCaseTest, …)
./gradlew :composeApp:desktopTest --tests "com.fleet.ecocar.map.*"
```

## Integration testing (Makefile, sibling repo)

Full CAN → BMS → EcoCar → UI pipeline is driven from **`bms-monitoring-app/Makefile`** (not this repo). Run from the BMS app directory:

```bash
cd ../bms-monitoring-app
make integration-test-ui
```

That target runs `build-install` (both APKs), sends test CAN frames (SOC **50%**), verifies IPC in logcat, then **`verify-ui-metrics`** on the emulator.

**Tips**

| Situation | What to do |
|-----------|------------|
| `battery-soc=50.0 not in UI dump` | Run `make build-install` in `bms-monitoring-app` so the emulator gets the latest EcoCar APK with automation descriptors |
| Low-battery dialog during `integration-test-ui` | Pack SOC is **50%** (above 20); low-SOC / dialog is `make test-low-soc` (`TEST_SOC_LOW := 10`) |
| `relay port 9999 not open` | First Lima run can be slow; retry `make relay` or wait up to **120 s** (`RELAY_READY_SECS`) |
| IPC bind fails | Same debug keystore on both APKs — see [Development](#development-local-machine--deviceemulator) |
| Stale emulator / ANR | `make shutdown` then rerun `make integration-test-ui` |

See [`bms-monitoring-app/README.md`](../bms-monitoring-app/README.md#integration-testing-makefile) for relay, clean/shutdown, and CSMS targets.
