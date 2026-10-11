package com.fleet.ecocar.ui.volume

import androidx.compose.runtime.Composable

data class MusicVolumeUi(
    val index: Int,
    val maxIndex: Int,
    val onIndexChange: (Int) -> Unit,
)

@Composable
expect fun rememberMusicVolume(): MusicVolumeUi
