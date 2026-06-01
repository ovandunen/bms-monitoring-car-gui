package com.fleet.ecocar.music

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val uri: Uri,
    val albumArtUri: Uri?,
    val durationMs: Long,
)

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val favicon: String?,
    val country: String,
    val genre: String,
    val bitrate: Int,
)

enum class MusicPlaybackSurface {
    USB,
    RADIO,
}
