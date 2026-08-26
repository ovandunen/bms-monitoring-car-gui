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
val mapTilerKey: String = localProperties.getProperty("maptiler.api_key") ?: ""

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
            api(compose.animation)
            api(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            implementation("androidx.datastore:datastore-preferences-core:1.1.1")
        }
        androidMain.dependencies {
            implementation(project(":bms-monitoring-ipc"))
            implementation(project(":eco-car-battery-ui"))
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
            implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
            implementation(libs.appcompat)
            implementation("androidx.datastore:datastore-preferences:1.1.1")
            implementation("org.maplibre.compose:maplibre-compose-android:0.12.1") {
                exclude(group = "org.maplibre.gl", module = "android-sdk-geojson")
                exclude(group = "org.maplibre.gl", module = "android-sdk-turf")
            }
            implementation("org.maplibre.gl:geojson:7.0.0-pre0")
            implementation("org.maplibre.gl:turf:7.0.0-pre0")
            implementation(libs.geckoview)
            implementation(libs.activity.compose)
            api(libs.media3.exoplayer)
            api(libs.media3.exoplayer.hls)
            api(libs.media3.session)
            api(libs.media3.ui)
            implementation(libs.glide)
            implementation(libs.androidx.media)
            implementation("com.google.android.gms:play-services-location:21.1.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
            implementation(libs.maplibre.navigation.core)
            implementation(libs.maplibre.navigation.ui.android)
            implementation(libs.graphhopper.core)
            implementation(libs.graphhopper.web.api)
            implementation(libs.timber)
        }
        named("desktopMain") {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
                implementation("junit:junit:4.13.2")
                implementation("androidx.arch.core:core-testing:2.2.0")
                implementation("org.robolectric:robolectric:4.14.1")
                implementation("androidx.test:core:1.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
            }
            kotlin.srcDir("${rootProject.projectDir}/androidApp/src/sharedTest/java/com/fleet/ecocar/navigation/support")
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("androidx.test.ext:junit:1.2.1")
                implementation("androidx.test:runner:1.6.2")
                implementation("androidx.test:core:1.6.1")
                implementation("androidx.compose.ui:ui-test-junit4")
                implementation("androidx.compose.ui:ui-test-manifest")
                implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
                implementation("com.google.android.material:material:1.12.0")
                implementation(libs.maplibre.navigation.core)
                implementation(libs.maplibre.navigation.ui.android)
                implementation(libs.activity.compose)
                implementation(libs.appcompat)
                implementation(compose.material3)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
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
        buildConfigField("String", "MAPTILER_API_KEY", "\"${mapTilerKey.escapeForBuildConfig()}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // Match :androidApp ABI splits — avoids ~677MB universal androidTest APK (GeckoView × 4 ABIs).
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    sourceSets {
        getByName("androidTest") {
            java.srcDir("src/androidInstrumentedTest/kotlin")
            java.srcDir("${rootProject.projectDir}/androidApp/src/sharedTest/java")
            manifest.srcFile("src/androidInstrumentedTest/AndroidManifest.xml")
        }
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
