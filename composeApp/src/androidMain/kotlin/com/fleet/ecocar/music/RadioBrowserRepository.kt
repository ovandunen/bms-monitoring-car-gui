package com.fleet.ecocar.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object RadioBrowserRepository {

    private const val GERMANY_URL =
        "https://de1.api.radio-browser.info/json/stations/bycountry/germany"

    suspend fun fetchGermanyStations(): List<RadioStation> = withContext(Dispatchers.IO) {
        val conn = (URL(GERMANY_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty("User-Agent", "EcoCarGUI/1.0 (com.fleet.ecocar)")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) return@withContext emptyList()
            parseStations(body)
        } finally {
            conn.disconnect()
        }
    }

    private fun parseStations(json: String): List<RadioStation> {
        val list = mutableListOf<RadioStation>()
        runCatching {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val url = o.optString("url_resolved").ifBlank { o.optString("url") }.trim()
                if (url.isBlank()) continue
                list.add(
                    RadioStation(
                        name = o.optString("name").ifBlank { "Sender" },
                        streamUrl = url,
                        favicon = o.optString("favicon").trim().takeIf { it.isNotEmpty() },
                        country = o.optString("country").ifBlank { "DE" },
                        genre = o.optString("tags").ifBlank { o.optString("codec", "") },
                        bitrate = o.optInt("bitrate"),
                    ),
                )
            }
        }
        return list
    }
}
