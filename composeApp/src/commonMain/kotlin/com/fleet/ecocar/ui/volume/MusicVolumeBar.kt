package com.fleet.ecocar.ui.volume

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.volume.indexFromSlider
import com.fleet.ecocar.volume.sliderFraction
import com.fleet.ecocar.volume.steppedVolumeIndex
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.volume_down
import eco_car_gui.composeapp.generated.resources.volume_slider
import eco_car_gui.composeapp.generated.resources.volume_speaker
import eco_car_gui.composeapp.generated.resources.volume_up
import org.jetbrains.compose.resources.stringResource

@Composable
fun MusicVolumeBar(
    index: Int,
    maxIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.volume_slider)
    Surface(
        modifier = modifier.fillMaxWidth().height(56.dp),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(Res.string.volume_speaker),
                    tint = EcoCarColors.GoldenYellow,
                )
            }
            IconButton(
                onClick = { onIndexChange(steppedVolumeIndex(index, -1, maxIndex)) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = stringResource(Res.string.volume_down),
                    tint = EcoCarColors.OnDark,
                )
            }
            Slider(
                value = sliderFraction(index, maxIndex),
                onValueChange = { onIndexChange(indexFromSlider(it, maxIndex)) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = sliderLabel },
                colors = SliderDefaults.colors(
                    thumbColor = EcoCarColors.GoldenYellow,
                    activeTrackColor = EcoCarColors.GoldenYellow,
                    inactiveTrackColor = EcoCarColors.OnDarkSecondary,
                ),
            )
            IconButton(
                onClick = { onIndexChange(steppedVolumeIndex(index, 1, maxIndex)) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.volume_up),
                    tint = EcoCarColors.OnDark,
                )
            }
        }
    }
}
