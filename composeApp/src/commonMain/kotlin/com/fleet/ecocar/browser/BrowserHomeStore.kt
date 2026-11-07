package com.fleet.ecocar.browser

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface BrowserHomeStore {
    val homeUrl: StateFlow<String>

    suspend fun setHomeUrl(raw: String)

    suspend fun resolvedHomeUrl(): String
}

class MemoryBrowserHomeStore(
    initial: String = BrowserHome.DEFAULT_URL,
) : BrowserHomeStore {
    private val current = MutableStateFlow(BrowserHome.resolve(initial))

    override val homeUrl: StateFlow<String> = current.asStateFlow()

    override suspend fun setHomeUrl(raw: String) {
        current.value = BrowserHome.resolve(raw)
    }

    override suspend fun resolvedHomeUrl(): String = current.value
}

class DataStoreBrowserHomeStore(
    private val dataStore: DataStore<Preferences>,
) : BrowserHomeStore {
    private val current = MutableStateFlow(BrowserHome.DEFAULT_URL)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val homeUrl: StateFlow<String> = current.asStateFlow()

    init {
        scope.launch {
            dataStore.data.collect { prefs ->
                current.value = BrowserHome.resolve(prefs[KEY])
            }
        }
    }

    override suspend fun setHomeUrl(raw: String) {
        val trimmed = raw.trim()
        dataStore.edit { prefs ->
            if (trimmed.isEmpty()) {
                prefs.remove(KEY)
            } else {
                prefs[KEY] = BrowserHome.resolve(trimmed)
            }
        }
    }

    override suspend fun resolvedHomeUrl(): String =
        BrowserHome.resolve(dataStore.data.first()[KEY])

    private companion object {
        val KEY = stringPreferencesKey("browser_home_url")
    }
}
