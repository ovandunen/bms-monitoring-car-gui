package com.fleet.ecocar.music

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.playbackModeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ecocar_playback_mode",
)

fun openPlaybackModeStore(context: Context): PlaybackModeStore =
    PlaybackModeStore(context.applicationContext.playbackModeDataStore)
