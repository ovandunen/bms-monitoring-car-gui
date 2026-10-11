package com.fleet.ecocar.music

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class PlaybackModeStore(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun load(): UsbPlaybackMode {
        val raw = dataStore.data.first()[KEY]
        return UsbPlaybackMode.fromStored(raw)
    }

    suspend fun save(mode: UsbPlaybackMode) {
        dataStore.edit { prefs -> prefs[KEY] = mode.name }
    }

    private companion object {
        val KEY = stringPreferencesKey("usb_playback_mode")
    }
}
