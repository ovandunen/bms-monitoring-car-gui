package com.fleet.ecocar.ui

import androidx.compose.ui.unit.dp

/**
 * Driver touch sizes for the Pixel Tablet (tangorpro), measured 2026-10-13:
 * reported density 320, physical 276 dpi. 64 dp is 11.8 mm on that glass.
 * 80 dp play/pause is 14.7 mm, inside both 76–96 dp and 12–15 mm.
 * A 24 dp gap is 4.4 mm. At the 160 dpi baseline these would be 10 mm, 12.7 mm, and 3.8 mm.
 * The volume slider is 30% under 5 cm: 271 dp * 0.7 = 190 dp, about 3.5 cm.
 * Play, previous, and next are 30% under the previous 56 dp and 45 dp targets.
 */
object DriverTouch {
    val MinTarget = 64.dp
    val PlayPause = 39.2.dp
    val MediaSkip = 31.5.dp
    val MediaIcon = 28.dp
    val TransportIcon = 19.6.dp
    val Gap = 24.dp
    val ScreenEdge = 24.dp
    val VolumeSliderMax = 189.7.dp
    val VolumeSliderHeight = 44.8.dp

    /**
     * Glass inset for the cover that clamps the tablet rim.
     * 24 dp is 4.4 mm on the Pixel Tablet measured 2026-10-13, so 48 dp is 8.8 mm.
     * The side menu and the top-right clock sit this far in from the glass.
     */
    val CoverInset = 48.dp

    /** How long the volume slider stays open after the last drag. */
    const val VolumeSliderOpenMs = 4_000L
}
