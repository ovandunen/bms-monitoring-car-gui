package com.fleet.ecocar.ui.bottom

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tripHintDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ecocar_trip_hint",
)

private val KEY_TRIP_RESET_HINT_SHOWN = booleanPreferencesKey("trip_reset_hint_shown")

class TripResetHintStore(context: Context) : TripResetHintRepository {
    private val dataStore = context.applicationContext.tripHintDataStore

    override suspend fun isHintShown(): Boolean =
        dataStore.data.map { it[KEY_TRIP_RESET_HINT_SHOWN] ?: false }.first()

    override suspend fun markHintShown() {
        dataStore.edit { it[KEY_TRIP_RESET_HINT_SHOWN] = true }
    }
}
