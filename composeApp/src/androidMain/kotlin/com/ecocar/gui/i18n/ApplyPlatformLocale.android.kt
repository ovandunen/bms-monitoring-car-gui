package com.ecocar.gui.i18n

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import java.util.Locale

@Composable
actual fun ApplyPlatformLocale(language: AppLanguage) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val locale = Locale.forLanguageTag(language.bcp47)
    Locale.setDefault(locale)
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.bcp47))
    val newConfig = Configuration(configuration)
    newConfig.setLocale(locale)
    val resources = context.resources
    @Suppress("DEPRECATION")
    resources.updateConfiguration(newConfig, resources.displayMetrics)
}
