package com.fleet.ecocar.music

import android.net.Uri

/** USB list scans run only while the USB tab is showing and audio permission is granted. */
fun shouldScanUsb(permissionGranted: Boolean, tabIsUsb: Boolean): Boolean =
    permissionGranted && tabIsUsb

fun usbTrackUriIsPresent(playingUri: Uri?, tracks: List<Track>): Boolean =
    playingUri != null && tracks.any { it.uri == playingUri }

/**
 * Stop only a USB session whose current item is gone after a rescan.
 * Radio uses the same player and is left running.
 */
fun shouldStopUsbAfterRescan(
    surface: MusicPlaybackSurface?,
    playingUri: Uri?,
    tracks: List<Track>,
): Boolean = surface == MusicPlaybackSurface.USB && !usbTrackUriIsPresent(playingUri, tracks)
