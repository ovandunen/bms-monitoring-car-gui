package com.fleet.ecocar.ui.volume

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverIconButton
import com.fleet.ecocar.ui.DriverTouch
import com.fleet.ecocar.volume.indexFromSlider
import com.fleet.ecocar.volume.sliderFraction
import com.fleet.ecocar.volume.steppedVolumeIndex
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.volume_down
import eco_car_gui.composeapp.generated.resources.volume_mute
import eco_car_gui.composeapp.generated.resources.volume_slider
import eco_car_gui.composeapp.generated.resources.volume_unmute
import eco_car_gui.composeapp.generated.resources.volume_up
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicVolumeBar(
    index: Int,
    maxIndex: Int,
    muted: Boolean,
    onIndexChange: (Int) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.volume_slider)
    val sliderInteraction = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(DriverTouch.MinTarget + 16.dp)
                .padding(horizontal = DriverTouch.ScreenEdge),
            horizontalArrangement = Arrangement.spacedBy(DriverTouch.Gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DriverIconButton(
                onClick = onToggleMute,
                contentDescription = stringResource(
                    if (muted) Res.string.volume_unmute else Res.string.volume_mute,
                ),
                icon = if (muted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                size = DriverTouch.MinTarget,
                highlighted = muted,
                restingTint = EcoCarColors.GoldenYellow,
            )
            DriverIconButton(
                onClick = { onIndexChange(steppedVolumeIndex(index, -1, maxIndex)) },
                contentDescription = stringResource(Res.string.volume_down),
                icon = Icons.Filled.Remove,
                size = DriverTouch.MinTarget,
            )
            Slider(
                value = sliderFraction(index, maxIndex),
                onValueChange = { onIndexChange(indexFromSlider(it, maxIndex)) },
                modifier = Modifier
                    .weight(1f)
                    .height(DriverTouch.MinTarget)
                    .semantics { contentDescription = sliderLabel },
                interactionSource = sliderInteraction,
                thumb = {
                    Box(
                        modifier = Modifier.size(DriverTouch.MinTarget),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(EcoCarColors.GoldenYellow, CircleShape),
                        )
                    }
                },
                colors = SliderDefaults.colors(
                    thumbColor = EcoCarColors.GoldenYellow,
                    activeTrackColor = EcoCarColors.GoldenYellow,
                    inactiveTrackColor = EcoCarColors.OnDarkSecondary,
                ),
            )
            DriverIconButton(
                onClick = { onIndexChange(steppedVolumeIndex(index, 1, maxIndex)) },
                contentDescription = stringResource(Res.string.volume_up),
                icon = Icons.Filled.Add,
                size = DriverTouch.MinTarget,
            )
        }
    }
}
