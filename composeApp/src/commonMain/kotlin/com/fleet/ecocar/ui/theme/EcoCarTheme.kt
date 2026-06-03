package com.fleet.ecocar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.fleet.ecocar.theme.EcoCarColors

private val EcoCarDarkScheme = darkColorScheme(
    primary = EcoCarColors.GoldenYellow,
    onPrimary = EcoCarColors.NearBlack,
    secondary = EcoCarColors.DarkGreenTile,
    onSecondary = EcoCarColors.OnDark,
    background = EcoCarColors.NearBlack,
    surface = EcoCarColors.SurfaceElevated,
    onBackground = EcoCarColors.OnDark,
    onSurface = EcoCarColors.OnDark,
    outline = EcoCarColors.Divider,
    error = Color(0xFFFF6B6B),
    onError = EcoCarColors.NearBlack,
)

@Composable
fun EcoCarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EcoCarDarkScheme,
        content = content,
    )
}
