package com.fleet.ecocar.ui.side

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ecocar.gui.nav.localizedLabel
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.sidebar_collapse
import eco_car_gui.composeapp.generated.resources.sidebar_expand
import org.jetbrains.compose.resources.stringResource

private val ExpandedWidth = 260.dp
private val CollapsedWidth = 64.dp

@Composable
fun EcoSideNav(
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    selected: MainDestination,
    onSelect: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.width(if (expanded) ExpandedWidth else CollapsedWidth).fillMaxHeight(),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
        ) {
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    },
                    contentDescription = stringResource(
                        if (expanded) Res.string.sidebar_collapse else Res.string.sidebar_expand,
                    ),
                    tint = EcoCarColors.GoldenYellow,
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(MainDestination.entries.toList()) { dest ->
                    NavTile(
                        destination = dest,
                        expanded = expanded,
                        selected = dest == selected,
                        onClick = { onSelect(dest) },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTile(
    destination: MainDestination,
    expanded: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val bg = if (selected) EcoCarColors.GoldenYellowMuted else EcoCarColors.DarkGreenTile
    val borderColor = if (selected) EcoCarColors.GoldenYellow else EcoCarColors.Divider
    val borderWidth = if (selected) 2.dp else 1.dp
    val contentColor = if (selected) EcoCarColors.GoldenYellow else EcoCarColors.OnDark

    val mod = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(bg)
        .border(borderWidth, borderColor, shape)
        .clickable(onClick = onClick)
        .padding(horizontal = 10.dp, vertical = 12.dp)

    if (expanded) {
        Row(
            modifier = mod,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                tint = contentColor,
            )
            Text(
                text = destination.localizedLabel(),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        Box(
            modifier = mod,
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.localizedLabel(),
                tint = contentColor,
            )
        }
    }
}
