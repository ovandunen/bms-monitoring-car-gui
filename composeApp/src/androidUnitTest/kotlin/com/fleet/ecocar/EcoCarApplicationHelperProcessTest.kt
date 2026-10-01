package com.fleet.ecocar

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class EcoCarProcessIdentityTest {

    @Test
    fun isMainProcess_matchesPackageName() {
        assertTrue(EcoCarProcessIdentity.isMainProcess("com.fleet.ecocar", "com.fleet.ecocar"))
    }

    @Test
    fun isMainProcess_helperProcessName_isFalse() {
        assertFalse(EcoCarProcessIdentity.isMainProcess("com.fleet.ecocar", "com.fleet.ecocar:gpu"))
        assertFalse(EcoCarProcessIdentity.isMainProcess("com.fleet.ecocar", "com.fleet.ecocar:tab17"))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28], application = GpuProcessEcoCarApplication::class)
class EcoCarApplicationHelperProcessTest {

    @Test
    fun helperProcess_doesNotCreateBmsConnection() {
        val app = RuntimeEnvironment.getApplication() as GpuProcessEcoCarApplication
        val batteryField = EcoCarApplication::class.java.getDeclaredField("batteryClient")
        batteryField.isAccessible = true
        val binderField = EcoCarApplication::class.java.getDeclaredField("bmsTelemetryBinder")
        binderField.isAccessible = true
        assertNull(batteryField.get(app))
        assertNull(binderField.get(app))
    }
}

class GpuProcessEcoCarApplication : EcoCarApplication() {
    override fun currentProcessName(): String = "${packageName}:gpu"
}
