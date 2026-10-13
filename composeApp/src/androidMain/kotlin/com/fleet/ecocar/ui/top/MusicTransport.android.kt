package com.fleet.ecocar.ui.top

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication

@Composable
actual fun rememberMusicTransport(): MusicTransportHandlers {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    return MusicTransportHandlers(
        onPrevious = app::transportPrevious,
        onPlayPause = app::toggleTransportPlayPause,
        onNext = app::transportNext,
        onResumeListening = app::resumeListening,
    )
}
