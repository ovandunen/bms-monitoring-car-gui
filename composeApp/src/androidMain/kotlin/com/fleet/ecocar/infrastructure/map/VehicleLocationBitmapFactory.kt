package com.fleet.ecocar.infrastructure.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.fleet.ecocar.composeapp.R

internal object VehicleLocationBitmapFactory {
    /** Pixel size of the MapLibre vehicle bitmap width. doubled on user request 2026-09-30 */
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
        val width = VEHICLE_ICON_SIZE_PX
        val height =
            if (intrinsicWidth <= 0) {
                width
            } else {
                (width * intrinsicHeight / intrinsicWidth).coerceAtLeast(1)
            }
        return width to height
    }
}
