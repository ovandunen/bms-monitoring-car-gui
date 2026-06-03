package com.ecocar.gui.i18n

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ecocar_language",
)

private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")

@Singleton
class AndroidLanguageRepository @Inject constructor(
    @ApplicationContext context: Context,
) : LanguageRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore = context.applicationContext.languageDataStore

    private val _selectedLanguage = MutableStateFlow(AppLanguage.DE)

    override val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    init {
        scope.launch {
            val stored = dataStore.data.first()[KEY_APP_LANGUAGE] ?: "de"
            _selectedLanguage.value = AppLanguage.fromCode(stored)
        }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[KEY_APP_LANGUAGE] = language.code }
        _selectedLanguage.value = language
    }

    override fun getDefault(): AppLanguage = AppLanguage.DE
}
