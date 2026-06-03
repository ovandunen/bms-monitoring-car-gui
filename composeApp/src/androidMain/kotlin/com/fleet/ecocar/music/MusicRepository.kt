package com.fleet.ecocar.music

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Facade for local USB/storage scanning and Radio Browser API.
 */
object MusicRepository {

    suspend fun loadLocalTracks(context: Context): List<Track> =
        UsbMediaScanner.scan(context)

    suspend fun loadGermanyRadioStations(): List<RadioStation> =
        RadioBrowserRepository.fetchGermanyStations()

    suspend fun loadAll(
        context: Context,
        includeLocal: Boolean = true,
        includeRadio: Boolean = true,
    ): Pair<List<Track>, List<RadioStation>> = coroutineScope {
        val local = if (includeLocal) async { UsbMediaScanner.scan(context) } else async { emptyList() }
        val radio = if (includeRadio) async { RadioBrowserRepository.fetchGermanyStations() } else async { emptyList() }
        Pair(local.await(), radio.await())
    }
}
