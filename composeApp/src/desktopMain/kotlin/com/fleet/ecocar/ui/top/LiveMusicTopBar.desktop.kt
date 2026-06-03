package com.fleet.ecocar.ui.top

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
actual fun rememberLiveMusicTopBarState(): TopBarMusicState {
    var clock by remember { mutableStateOf(formatDeskClock()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            clock = formatDeskClock()
        }
    }
    return remember(clock) {
        TopBarMusicState(
            title = "Mama Africa – Chico César",
            duration = "2:13",
            source = "USB 1",
            clock = clock,
        )
    }
}

private fun formatDeskClock(): String =
    SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
