package com.fleet.ecocar.browser

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.browserHomeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ecocar_browser_home",
)

fun openBrowserHomeStore(context: Context): BrowserHomeStore =
    DataStoreBrowserHomeStore(context.applicationContext.browserHomeDataStore)
