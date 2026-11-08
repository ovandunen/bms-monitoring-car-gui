package com.fleet.ecocar.ui.top

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverTouch
import com.fleet.ecocar.ui.volume.MusicVolumeUi
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.call_music_paused
import eco_car_gui.composeapp.generated.resources.call_resume
import org.jetbrains.compose.resources.stringResource

data class TopBarMusicState(
    val title: String = "—",
    val duration: String = "0:00 / --:--",
    val source: String = "",
    val clock: String = "—",
    val isPlaying: Boolean,
    val currentPosition: Long,
    val callPaused: Boolean = false,
    val hasMedia: Boolean = false,
    val canSkip: Boolean = false,
    val durationMs: Long = 0L,
) {
    constructor(clock: String) : this(
        title = "",
        duration = "0:00 / --:--",
        source = "",
        clock = clock,
        isPlaying = false,
        currentPosition = 0L,
    )
}

data class MusicTransportHandlers(
    val onPrevious: () -> Unit = {},
    val onPlayPause: () -> Unit = {},
    val onNext: () -> Unit = {},
    val onResumeListening: () -> Unit = {},
    val onSeek: (Long) -> Unit = {},
)

@Composable
fun EcoTopBar(
    music: TopBarMusicState,
    transport: MusicTransportHandlers,
    volume: MusicVolumeUi,
    volumeOpen: Boolean,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Column {
            NowPlayingStrip(
                music = music,
                transport = transport,
                volume = volume,
                volumeOpen = volumeOpen,
                onVolumeChange = onVolumeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DriverTouch.Gap, vertical = 8.dp),
            )
            if (music.callPaused) {
                CallPausedBanner(onResume = transport.onResumeListening)
            }
            HorizontalDivider(color = EcoCarColors.Divider, thickness = 1.dp)
        }
    }
}

@Composable
private fun CallPausedBanner(onResume: () -> Unit) {
    val label = stringResource(Res.string.call_resume)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DriverTouch.Gap)
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DriverTouch.Gap),
    ) {
        Text(
            text = stringResource(Res.string.call_music_paused),
            style = MaterialTheme.typography.titleMedium,
            color = EcoCarColors.OnDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .height(DriverTouch.MinTarget)
                .widthIn(min = DriverTouch.MinTarget)
                .clip(RoundedCornerShape(12.dp))
                .background(if (pressed) EcoCarColors.OnDark else EcoCarColors.GoldenYellow)
                .border(
                    width = if (focused) 3.dp else 0.dp,
                    color = EcoCarColors.OnDark,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(
                    interactionSource = interaction,
                    indication = LocalIndication.current,
                    role = Role.Button,
                    onClick = onResume,
                )
                .semantics {
                    role = Role.Button
                    contentDescription = label
                }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = if (pressed) EcoCarColors.GoldenYellow else EcoCarColors.NearBlack,
            )
        }
    }
}
