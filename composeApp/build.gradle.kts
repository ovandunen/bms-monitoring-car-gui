import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.plugin.parcelize")
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.reader(Charsets.UTF_8)?.use { load(it) }
}
val mapTilerKey: String = localProperties.getProperty("maptiler.key") ?: ""

fun String.escapeForBuildConfig(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

kotlin {
    jvmToolchain(21)

    androidTarget()

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            api(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            implementation("androidx.datastore:datastore-preferences-core:1.1.1")
        }
        androidMain.dependencies {
            implementation(project(":bms-monitoring-ipc"))
            implementation(project(":eco-car-battery-ui"))
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
            implementation(libs.appcompat)
            implementation("androidx.datastore:datastore-preferences:1.1.1")
            implementation(libs.maplibre.compose.android)
            implementation(libs.geckoview)
            implementation(libs.activity.compose)
            api(libs.media3.exoplayer)
            api(libs.media3.exoplayer.hls)
            api(libs.media3.session)
            api(libs.media3.ui)
            implementation(libs.glide)
            implementation(libs.androidx.media)
            implementation("com.google.android.gms:play-services-location:21.1.0")
        }
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

android {
    namespace = "com.fleet.ecocar.composeapp"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("src/androidMain/consumer-rules.pro")
        buildConfigField("String", "MAPTILER_KEY", "\"${mapTilerKey.escapeForBuildConfig()}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
        aidl = true
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "eco_car_gui.composeapp.generated.resources"
}

compose.desktop {
    application {
        mainClass = "com.fleet.ecocar.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EcoCar GUI"
            packageVersion = "1.0.0"
        }
    }
}
