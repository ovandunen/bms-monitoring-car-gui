package com.fleet.ecocar

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ecocar.gui.i18n.LanguageRepository
import com.fleet.ecocar.canlog.CanLogButton
import com.fleet.ecocar.composeapp.BuildConfig
import com.fleet.ecocar.ui.EcoCarApp
import com.fleet.ecocar.ui.settings.SettingsVersionLabel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var languageRepository: LanguageRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val versionLabel = SettingsVersionLabel.text(
            guiVersion = packageVersionName(this, packageName).orEmpty(),
            bmsVersion = packageVersionName(this, SettingsVersionLabel.BMS_PACKAGE),
        )
        setContent {
            EcoCarApp(
                languageRepository = languageRepository,
                versionLabel = versionLabel,
                onToggleCanLog = if (BuildConfig.DEBUG) {
                    { CanLogButton.press(this@MainActivity) }
                } else {
                    null
                },
            )
        }
    }
}

internal fun packageVersionName(context: Context, packageName: String): String? =
    try {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(packageName, 0)
        }
        info.versionName
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }
