package com.fleet.ecocar.infrastructure.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import com.fleet.ecocar.composeapp.R

internal object ChargingPinBitmapFactory {

    private const val ICON_SIZE_PX = 48

    fun createBitmap(context: Context): Bitmap {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_charging_pin)
            ?: error("ic_charging_pin drawable missing")
        val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, ICON_SIZE_PX, ICON_SIZE_PX)
        drawable.draw(canvas)
        return bitmap
    }
}
