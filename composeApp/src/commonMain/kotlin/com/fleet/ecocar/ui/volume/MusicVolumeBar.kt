package com.fleet.ecocar.ui.volume

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    val switchSize = DriverTouch.PlayPause
    Row(
        modifier = modifier.height(switchSize),
        horizontalArrangement = Arrangement.spacedBy(DriverTouch.Gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DriverIconButton(
            onClick = onToggleMute,
            contentDescription = stringResource(
                if (muted) Res.string.volume_unmute else Res.string.volume_mute,
            ),
            icon = if (muted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            size = switchSize,
            iconSize = DriverTouch.MediaIcon,
            highlighted = muted,
            restingTint = EcoCarColors.GoldenYellow,
        )
        DriverIconButton(
            onClick = { onIndexChange(steppedVolumeIndex(index, -1, maxIndex)) },
            contentDescription = stringResource(Res.string.volume_down),
            icon = Icons.AutoMirrored.Filled.VolumeDown,
            size = switchSize,
            iconSize = DriverTouch.MediaIcon,
        )
        Slider(
            value = sliderFraction(index, maxIndex),
            onValueChange = { onIndexChange(indexFromSlider(it, maxIndex)) },
            modifier = Modifier
                .width(DriverTouch.VolumeSliderMax)
                .semantics { contentDescription = sliderLabel },
            colors = SliderDefaults.colors(
                thumbColor = EcoCarColors.GoldenYellow,
                activeTrackColor = EcoCarColors.GoldenYellow,
                inactiveTrackColor = EcoCarColors.OnDarkSecondary,
            ),
        )
        DriverIconButton(
            onClick = { onIndexChange(steppedVolumeIndex(index, 1, maxIndex)) },
            contentDescription = stringResource(Res.string.volume_up),
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            size = switchSize,
            iconSize = DriverTouch.MediaIcon,
        )
    }
}
