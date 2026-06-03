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
