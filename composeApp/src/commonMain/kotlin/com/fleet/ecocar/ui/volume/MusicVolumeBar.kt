package com.fleet.ecocar.ui.volume

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverIconButton
import com.fleet.ecocar.ui.DriverTouch
import com.fleet.ecocar.volume.indexFromSlider
import com.fleet.ecocar.volume.sliderFraction
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.volume_adjust
import eco_car_gui.composeapp.generated.resources.volume_back_to_playback
import eco_car_gui.composeapp.generated.resources.volume_slider
import org.jetbrains.compose.resources.stringResource

@Composable
fun VolumeSpeakerButton(
    volumeOff: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DriverIconButton(
        onClick = onClick,
        onLongClick = onLongClick,
        contentDescription = stringResource(
            if (volumeOff) Res.string.volume_back_to_playback else Res.string.volume_adjust,
        ),
        icon = if (volumeOff) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
        size = DriverTouch.MinTarget,
        iconSize = DriverTouch.MediaIcon,
        restingTint = EcoCarColors.GoldenYellow,
        modifier = modifier,
    )
}

@Composable
fun VolumeLevelSlider(
    index: Int,
    maxIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.volume_slider)
    Slider(
        value = sliderFraction(index, maxIndex),
        onValueChange = { onIndexChange(indexFromSlider(it, maxIndex)) },
        modifier = modifier
            .fillMaxWidth()
            .height(DriverTouch.VolumeSliderHeight)
            .semantics { contentDescription = sliderLabel },
        colors = SliderDefaults.colors(
            thumbColor = EcoCarColors.GoldenYellow,
            activeTrackColor = EcoCarColors.GoldenYellow,
            inactiveTrackColor = EcoCarColors.OnDarkSecondary,
        ),
    )
}
