import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.2.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

val integrationContract = file("../bms-monitoring-app/integration-test.contract.properties")

subprojects {
    tasks.withType<Test>().configureEach {
        if (integrationContract.exists()) {
            systemProperty("integration.contract.file", integrationContract.absolutePath)
        }
    }
    // includeBuild IPC: AGP lintVitalAnalyzeRelease reads
    // bms-monitoring-ipc/build/intermediates/lint_model_metadata/... which is not
    // a declared dependency unless we wire it.
    tasks.configureEach {
        if (name.startsWith("lint")) {
            dependsOn(gradle.includedBuild("bms-monitoring-ipc").task(":writeReleaseLintModelMetadata"))
            dependsOn(gradle.includedBuild("bms-monitoring-ipc").task(":writeDebugLintModelMetadata"))
        }
    }
}
