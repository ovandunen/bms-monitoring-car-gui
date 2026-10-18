package com.fleet.ecocar.ui.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsVersionLabelTest {

    @Test
    fun text_whenTagsMatch_showsTheSharedTag() {
        assertEquals("v2.0.0.0rc", SettingsVersionLabel.text("v2.0.0.0rc", "v2.0.0.0rc"))
    }

    @Test
    fun text_whenBmsTagDiffers_showsBoth() {
        assertEquals(
            "v2.0.0.0rc · BMS v0.3.0.0rc",
            SettingsVersionLabel.text("v2.0.0.0rc", "v0.3.0.0rc"),
        )
    }

    @Test
    fun text_whenBmsIsMissing_showsTheGuiTag() {
        assertEquals("v2.0.0.0rc", SettingsVersionLabel.text("v2.0.0.0rc", null))
        assertEquals("v2.0.0.0rc", SettingsVersionLabel.text(" v2.0.0.0rc ", "  "))
    }

    @Test
    fun text_whenGuiTagIsBlank_showsNothing() {
        assertEquals("", SettingsVersionLabel.text("  ", "v2.0.0.0rc"))
    }
}
