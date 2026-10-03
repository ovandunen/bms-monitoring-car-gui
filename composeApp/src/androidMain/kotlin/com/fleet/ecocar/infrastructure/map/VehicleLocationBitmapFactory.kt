package com.fleet.ecocar.infrastructure.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.fleet.ecocar.composeapp.R

internal object VehicleLocationBitmapFactory {
    /** Longest side of the MapLibre vehicle bitmap, in pixels. Doubled on user request 2026-09-30. */
    const val VEHICLE_ICON_SIZE_PX = 96

    fun createBitmap(context: Context): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_vehicle_location)
            ?: error("ic_vehicle_location drawable missing")
        return rasterize(drawable)
    }

    private fun rasterize(drawable: Drawable): Bitmap {
        val (width, height) = bitmapSizePx(drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }

    internal fun bitmapSizePx(intrinsicWidth: Int, intrinsicHeight: Int): Pair<Int, Int> {
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            return VEHICLE_ICON_SIZE_PX to VEHICLE_ICON_SIZE_PX
        }
        return if (intrinsicWidth >= intrinsicHeight) {
            val height = (VEHICLE_ICON_SIZE_PX.toLong() * intrinsicHeight / intrinsicWidth)
                .toInt()
                .coerceAtLeast(1)
            VEHICLE_ICON_SIZE_PX to height
        } else {
            val width = (VEHICLE_ICON_SIZE_PX.toLong() * intrinsicWidth / intrinsicHeight)
                .toInt()
                .coerceAtLeast(1)
            width to VEHICLE_ICON_SIZE_PX
        }
    }
}
