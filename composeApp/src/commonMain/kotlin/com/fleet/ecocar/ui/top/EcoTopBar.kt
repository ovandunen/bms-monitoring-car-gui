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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import com.fleet.ecocar.ui.DriverIconButton
import com.fleet.ecocar.ui.DriverTouch
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.call_music_paused
import eco_car_gui.composeapp.generated.resources.call_resume
import eco_car_gui.composeapp.generated.resources.media_next
import eco_car_gui.composeapp.generated.resources.media_pause
import eco_car_gui.composeapp.generated.resources.media_play
import eco_car_gui.composeapp.generated.resources.media_previous
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
)

@Composable
fun EcoTopBar(
    music: TopBarMusicState,
    transport: MusicTransportHandlers,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DriverTouch.ScreenEdge, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = music.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = EcoCarColors.OnDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(
                            text = music.duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = EcoCarColors.OnDarkSecondary,
                        )
                        Text(
                            text = music.source,
                            style = MaterialTheme.typography.bodySmall,
                            color = EcoCarColors.GoldenYellow,
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(horizontal = DriverTouch.Gap),
                    horizontalArrangement = Arrangement.spacedBy(DriverTouch.Gap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DriverIconButton(
                        onClick = transport.onPrevious,
                        contentDescription = stringResource(Res.string.media_previous),
                        icon = Icons.Filled.SkipPrevious,
                        size = DriverTouch.MinTarget,
                        enabled = music.canSkip,
                    )
                    DriverIconButton(
                        onClick = transport.onPlayPause,
                        contentDescription = stringResource(
                            if (music.isPlaying) Res.string.media_pause else Res.string.media_play,
                        ),
                        icon = if (music.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        size = DriverTouch.PlayPause,
                        highlighted = music.isPlaying,
                        enabled = music.hasMedia,
                        restingTint = EcoCarColors.GoldenYellow,
                        iconSize = 40.dp,
                    )
                    DriverIconButton(
                        onClick = transport.onNext,
                        contentDescription = stringResource(Res.string.media_next),
                        icon = Icons.Filled.SkipNext,
                        size = DriverTouch.MinTarget,
                        enabled = music.canSkip,
                    )
                }
                Text(
                    text = music.clock,
                    style = MaterialTheme.typography.headlineSmall,
                    color = EcoCarColors.GoldenYellow,
                )
            }
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
            .padding(horizontal = DriverTouch.ScreenEdge)
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
