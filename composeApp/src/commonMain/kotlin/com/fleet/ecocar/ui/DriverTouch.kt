package com.fleet.ecocar.ui

import androidx.compose.ui.unit.dp

/**
 * Driver touch sizes for the Pixel Tablet (tangorpro), measured 2026-10-13:
 * reported density 320, physical 276 dpi. 64 dp is 11.8 mm on that glass.
 * 80 dp play/pause is 14.7 mm, inside both 76–96 dp and 12–15 mm.
 * A 24 dp gap is 4.4 mm. At the 160 dpi baseline these would be 10 mm, 12.7 mm, and 3.8 mm.
 */
object DriverTouch {
    val MinTarget = 64.dp
    val PlayPause = 80.dp
    val Gap = 24.dp
    val ScreenEdge = 24.dp
}
