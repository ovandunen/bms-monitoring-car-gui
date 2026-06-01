package com.ecocar.gui.i18n

import androidx.compose.runtime.Composable
import java.util.Locale

@Composable
actual fun ApplyPlatformLocale(language: AppLanguage) {
    Locale.setDefault(Locale.forLanguageTag(language.bcp47))
}
