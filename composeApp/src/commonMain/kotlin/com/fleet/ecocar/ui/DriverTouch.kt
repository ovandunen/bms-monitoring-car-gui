package com.fleet.ecocar.ui

import androidx.compose.ui.unit.dp

/**
 * Driver touch sizes for the Pixel Tablet (tangorpro), measured 2026-10-13:
 * reported density 320, physical 276 dpi. 64 dp is 11.8 mm on that glass.
 * 80 dp play/pause is 14.7 mm, inside both 76–96 dp and 12–15 mm.
 * A 24 dp gap is 4.4 mm. At the 160 dpi baseline these would be 10 mm, 12.7 mm, and 3.8 mm.
 * The volume slider was at most 30 mm on that glass: 64 dp / 11.8 mm * 30 mm = 163 dp.
 * The top-bar slider, volume switches, and play buttons are 30% smaller than those targets.
 */
object DriverTouch {
    val MinTarget = 64.dp
    val PlayPause = 56.dp
    val MediaSkip = 45.dp
    val MediaIcon = 28.dp
    val Gap = 24.dp
    val ScreenEdge = 24.dp
    val VolumeSliderMax = 114.dp
}
