package com.fleet.ecocar.ui.side

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ecocar.gui.nav.localizedLabel
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverTouch
import com.fleet.ecocar.ui.volume.VolumeSpeakerButton

/** Wide enough for a label and a [DriverTouch.MinTarget] hit area, without taking map height. */
internal val SideNavWidth = DriverTouch.MinTarget + 48.dp

@Composable
fun EcoSideNav(
    selected: MainDestination,
    onSelect: (MainDestination) -> Unit,
    volumeOff: Boolean,
    onVolumeClick: () -> Unit,
    onVolumeLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight().width(SideNavWidth),
        containerColor = EcoCarColors.SurfaceElevated,
        contentColor = EcoCarColors.OnDark,
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            VolumeSpeakerButton(
                volumeOff = volumeOff,
                onClick = onVolumeClick,
                onLongClick = onVolumeLongClick,
            )
        }
        MainDestination.railEntries.forEach { destination ->
            val label = destination.localizedLabel()
            NavigationRailItem(
                selected = destination == selected,
                onClick = { onSelect(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                        modifier = Modifier.size(DriverTouch.MediaIcon),
                    )
                },
                label = {
                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = EcoCarColors.NearBlack,
                    selectedTextColor = EcoCarColors.GoldenYellow,
                    indicatorColor = EcoCarColors.GoldenYellow,
                    unselectedIconColor = EcoCarColors.OnDark,
                    unselectedTextColor = EcoCarColors.OnDarkSecondary,
                ),
            )
        }
    }
}
