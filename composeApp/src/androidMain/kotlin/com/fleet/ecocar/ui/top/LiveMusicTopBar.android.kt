package com.fleet.ecocar.ui.top

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication

@Composable
actual fun rememberLiveMusicTopBarState(): TopBarMusicState {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val state by app.topBarMusicState.collectAsState(initial = TopBarMusicState())
    return state
}
