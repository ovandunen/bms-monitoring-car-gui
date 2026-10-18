package com.fleet.ecocar.ui.settings

/**
 * Version text for Settings. Both apps take versionName from the same git tag.
 * One tag means they match. Two tags means the tablet copies differ.
 */
object SettingsVersionLabel {
    const val BMS_PACKAGE = "ch.ecocarsolaire.bms"

    fun text(guiVersion: String, bmsVersion: String?): String {
        val gui = guiVersion.trim()
        if (gui.isEmpty()) return ""
        val bms = bmsVersion?.trim().orEmpty()
        return if (bms.isEmpty() || bms == gui) gui else "$gui · BMS $bms"
    }
}
