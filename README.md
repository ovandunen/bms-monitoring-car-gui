# bms-monitoring-car-gui

EcoCar driver GUI and shared battery UI for the BMS monitoring stack.

## Modules

| Module | Role |
|--------|------|
| `:eco-car-battery-ui` | Shared Android library — `BatteryOverviewScreen` / `BatteryOverviewUiModel` |
| `:composeApp` | KMP library (`androidTarget` + JVM desktop), namespace `com.fleet.ecocar` — `EcoCarApp`, `MainContentArea`, `BatterySubNav`, `BmsTelemetryBinder`, `DemoBatteryModels`, … |
| `:androidApp` | Android application (`applicationId` `com.fleet.ecocar`) — `MainActivity`, `EcoCarGuiApplication` (Hilt), depends on `:composeApp` |

## Publish battery UI to local Maven

```bash
./gradlew :eco-car-battery-ui:publishReleasePublicationToMavenLocal
```

Artifact: `com.fleet.shared:eco-car-battery-ui:1.0.0`

## Build & run

```bash
./gradlew :androidApp:assembleDebug
./gradlew :composeApp:run
```

Optional MapTiler key for the charging-station map (not committed):

```properties
# local.properties
maptiler.key=YOUR_KEY
```

## Consumers

- `bms-monitoring-app` (`com.fleet.bms`) — BMS service + AIDL IPC
- `androidApp` (`com.fleet.ecocar`) — EcoCar GUI APK (same signing as BMS for IPC)
