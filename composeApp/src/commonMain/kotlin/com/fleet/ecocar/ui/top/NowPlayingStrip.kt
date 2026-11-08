package com.fleet.ecocar.ui.top

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverIconButton
import com.fleet.ecocar.ui.DriverTouch
import com.fleet.ecocar.ui.volume.MusicVolumeUi
import com.fleet.ecocar.ui.volume.VolumeLevelSlider
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.media_next
import eco_car_gui.composeapp.generated.resources.media_pause
import eco_car_gui.composeapp.generated.resources.media_play
import eco_car_gui.composeapp.generated.resources.media_previous
import eco_car_gui.composeapp.generated.resources.media_seek
import org.jetbrains.compose.resources.stringResource

@Composable
fun NowPlayingStrip(
    music: TopBarMusicState,
    transport: MusicTransportHandlers,
    volume: MusicVolumeUi,
    volumeOpen: Boolean,
    onVolumeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val seekLabel = stringResource(Res.string.media_seek)
    val seekable = music.hasMedia && music.durationMs > 0L
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val played = if (seekable) {
        (music.currentPosition.toFloat() / music.durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DriverTouch.Gap),
        ) {
            Text(
                text = music.title.ifBlank { "—" },
                style = MaterialTheme.typography.titleMedium,
                color = EcoCarColors.OnDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = music.duration,
                style = MaterialTheme.typography.bodySmall,
                color = EcoCarColors.OnDarkSecondary,
                maxLines = 1,
            )
            if (music.source.isNotBlank()) {
                Text(
                    text = music.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = EcoCarColors.GoldenYellow,
                    maxLines = 1,
                )
            }
            Text(
                text = music.clock,
                style = MaterialTheme.typography.headlineSmall,
                color = EcoCarColors.GoldenYellow,
                maxLines = 1,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(DriverTouch.MinTarget),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DriverTouch.Gap),
        ) {
            if (volumeOpen) {
                VolumeLevelSlider(
                    index = volume.index,
                    maxIndex = volume.maxIndex,
                    onIndexChange = onVolumeChange,
                )
                Spacer(Modifier.weight(1f))
            } else if (seekable) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = DriverTouch.Gap),
                ) {
                    Slider(
                        value = if (dragging) dragFraction else played,
                        onValueChange = { fraction ->
                            dragging = true
                            dragFraction = fraction
                            transport.onSeek((fraction * music.durationMs).toLong())
                        },
                        onValueChangeFinished = { dragging = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = seekLabel },
                        colors = SliderDefaults.colors(
                            thumbColor = EcoCarColors.GoldenYellow,
                            activeTrackColor = EcoCarColors.GoldenYellow,
                            inactiveTrackColor = EcoCarColors.OnDarkSecondary,
                        ),
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(DriverTouch.Gap)) {
            DriverIconButton(
                onClick = transport.onPrevious,
                contentDescription = stringResource(Res.string.media_previous),
                icon = Icons.Filled.SkipPrevious,
                size = DriverTouch.MediaSkip,
                iconSize = DriverTouch.TransportIcon,
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
                iconSize = DriverTouch.TransportIcon,
            )
            DriverIconButton(
                onClick = transport.onNext,
                contentDescription = stringResource(Res.string.media_next),
                icon = Icons.Filled.SkipNext,
                size = DriverTouch.MediaSkip,
                iconSize = DriverTouch.TransportIcon,
                enabled = music.canSkip,
            )
            }
        }
    }
}
