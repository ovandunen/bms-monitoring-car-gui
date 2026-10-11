package com.fleet.ecocar.ui.volume

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.fleet.ecocar.volume.DesktopMusicVolume

@Composable
actual fun rememberMusicVolume(): MusicVolumeUi {
    val control = remember { DesktopMusicVolume() }
    var level by remember { mutableStateOf(control.read()) }
    return MusicVolumeUi(
        index = level.index,
        maxIndex = level.maxIndex,
        onIndexChange = { next ->
            control.setIndex(next)
            level = control.read()
        },
    )
}
