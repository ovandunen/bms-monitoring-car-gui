package com.ecocar.gui.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Desktop v1: in-memory stub; persists for session only. */
object DesktopLanguageRepository : LanguageRepository {
    private val _selectedLanguage = MutableStateFlow(AppLanguage.DE)
    override val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
    }

    override fun getDefault(): AppLanguage = AppLanguage.DE
}
