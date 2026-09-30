package com.fleet.ecocar.infrastructure.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import com.fleet.ecocar.composeapp.R

internal object VehicleLocationBitmapFactory {
    /** Pixel size of the MapLibre vehicle bitmap. doubled on user request 2026-09-30 */
    const val VEHICLE_ICON_SIZE_PX = 96

    fun createBitmap(context: Context): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_vehicle_location)
            ?: error("ic_vehicle_location drawable missing")
        val bitmap = Bitmap.createBitmap(VEHICLE_ICON_SIZE_PX, VEHICLE_ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, VEHICLE_ICON_SIZE_PX, VEHICLE_ICON_SIZE_PX)
        drawable.draw(canvas)
        return bitmap
    }
}