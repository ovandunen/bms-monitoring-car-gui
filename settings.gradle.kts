pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://maven.mozilla.org/maven2/")
    }
}

rootProject.name = "bms-monitoring-car-gui"
include(":composeApp")
include(":androidApp")
include(":eco-car-battery-ui")
include(":bms-monitoring-ipc")          // standalone project
project(":bms-monitoring-ipc").projectDir = file("../bms-monitoring-ipc")
