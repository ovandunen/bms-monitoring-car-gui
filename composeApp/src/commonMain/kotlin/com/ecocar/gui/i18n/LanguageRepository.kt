package com.ecocar.gui.i18n

import kotlinx.coroutines.flow.StateFlow

interface LanguageRepository {
    val selectedLanguage: StateFlow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    fun getDefault(): AppLanguage = AppLanguage.DE
}
