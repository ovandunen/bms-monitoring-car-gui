package com.fleet.ecocar.browser

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

class BrowserHomeTest {

    @Test
    fun resolve_blankUsesStartpage() {
        assertEquals(BrowserHome.DEFAULT_URL, BrowserHome.resolve(null))
        assertEquals(BrowserHome.DEFAULT_URL, BrowserHome.resolve("  "))
    }

    @Test
    fun resolve_addsHttpsWhenSchemeIsMissing() {
        assertEquals("https://example.com", BrowserHome.resolve("example.com"))
        assertEquals("http://example.com", BrowserHome.resolve("http://example.com"))
    }

    @Test
    fun resolve_emptyUsesConfiguredFallback() {
        assertEquals("https://fleet.example", BrowserHome.resolve("", "https://fleet.example"))
    }

    @Test
    fun dataStore_blankClearsBackToStartpage() {
        val dir = java.io.File(
            System.getProperty("java.io.tmpdir"),
            "browser-home-${System.nanoTime()}",
        )
        dir.mkdirs()
        val file = java.io.File(dir, "home.preferences_pb")
        val store = DataStoreBrowserHomeStore(
            PreferenceDataStoreFactory.createWithPath(produceFile = { file.absolutePath.toPath() }),
        )
        try {
            runBlocking {
                assertEquals(BrowserHome.DEFAULT_URL, store.resolvedHomeUrl())
                store.setHomeUrl("fleet.example")
                assertEquals("https://fleet.example", store.resolvedHomeUrl())
                store.setHomeUrl("  ")
                assertEquals(BrowserHome.DEFAULT_URL, store.resolvedHomeUrl())
            }
        } finally {
            dir.deleteRecursively()
        }
    }
}
