package com.fleet.ecocar.ui.volume

import android.media.AudioManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.volume.AndroidMusicVolume

@Composable
actual fun rememberMusicVolume(): MusicVolumeUi {
    val context = LocalContext.current
    val control = remember {
        AndroidMusicVolume(
            context = context.applicationContext,
            audioManager = context.getSystemService(AudioManager::class.java),
        )
    }
    var level by remember { mutableStateOf(control.read()) }
    DisposableEffect(control) {
        val subscription = control.observe { level = it }
        onDispose { subscription.cancel() }
    }
    return MusicVolumeUi(
        index = level.index,
        maxIndex = level.maxIndex,
        onIndexChange = { next ->
            control.setIndex(next)
            level = control.read()
        },
    )
}
