package com.ecocar.gui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.key

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.DE }

@Composable
fun AppLocaleEnvironment(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppLanguage provides language) {
        key(language.code) {
            ApplyPlatformLocale(language)
            content()
        }
    }
}

@Composable
expect fun ApplyPlatformLocale(language: AppLanguage)
