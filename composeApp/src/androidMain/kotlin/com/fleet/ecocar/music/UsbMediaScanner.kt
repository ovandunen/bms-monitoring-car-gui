package com.fleet.ecocar.music

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Albums
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UsbMediaScanner {

    suspend fun scan(context: Context): List<Track> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.MIME_TYPE,
        )

        val mimeFilter = buildString {
            append("${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (")
            append("${MediaStore.Audio.Media.MIME_TYPE} IN (?, ?, ?, ?) OR ")
            append("${MediaStore.Audio.Media.MIME_TYPE} LIKE ? OR ")
            append("${MediaStore.Audio.Media.MIME_TYPE} LIKE ?)")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                append(" AND ${MediaStore.Audio.Media.IS_PENDING} = 0")
            }
        }

        val mimeArgs = arrayOf(
            "audio/mpeg",
            "audio/flac",
            "audio/aac",
            "audio/mp4",
            "audio/x-%",
            "audio/vnd.%",
        )

        resolver.query(
            collection,
            projection,
            mimeFilter,
            mimeArgs,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol).orEmpty().ifBlank { "Unbekannt" }
                val artist = cursor.getString(artistCol).orEmpty().ifBlank { "Unbekannt" }
                val album = cursor.getString(albumCol).orEmpty().ifBlank { "Album" }
                val durationMs = cursor.getLong(durCol).coerceAtLeast(0L)
                val albumId = cursor.getLong(albumIdCol)
                val trackUri = ContentUris.withAppendedId(collection, id)
                val artUri = if (albumId > 0) {
                    ContentUris.withAppendedId(Albums.EXTERNAL_CONTENT_URI, albumId)
                } else {
                    null
                }
                tracks.add(
                    Track(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        uri = trackUri,
                        albumArtUri = artUri,
                        durationMs = durationMs,
                    ),
                )
            }
        }
        tracks
    }
}
