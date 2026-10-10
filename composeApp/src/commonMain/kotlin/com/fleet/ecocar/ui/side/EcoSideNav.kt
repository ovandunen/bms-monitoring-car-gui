package com.fleet.ecocar.ui.side

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ecocar.gui.nav.localizedLabel
import com.fleet.ecocar.nav.MainDestination
import com.fleet.ecocar.theme.EcoCarColors

private val SideNavWidth = 56.dp
private val NavTargetSize = 48.dp

@Composable
fun EcoSideNav(
    selected: MainDestination,
    onSelect: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.width(SideNavWidth).fillMaxHeight(),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(MainDestination.entries.toList()) { dest ->
                NavTile(
                    destination = dest,
                    selected = dest == selected,
                    onClick = { onSelect(dest) },
                )
            }
        }
    }
}

@Composable
private fun NavTile(
    destination: MainDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val bg = if (selected) EcoCarColors.GoldenYellowMuted else EcoCarColors.DarkGreenTile
    val borderColor = if (selected) EcoCarColors.GoldenYellow else EcoCarColors.Divider
    val borderWidth = if (selected) 2.dp else 1.dp
    val contentColor = if (selected) EcoCarColors.GoldenYellow else EcoCarColors.OnDark
    val label = destination.localizedLabel()

    Box(
        modifier = Modifier
            .size(NavTargetSize)
            .clip(shape)
            .background(bg)
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = null,
            tint = contentColor,
        )
    }
}
