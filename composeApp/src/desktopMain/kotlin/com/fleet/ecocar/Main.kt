package com.fleet.ecocar

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ecocar.gui.i18n.DesktopLanguageRepository
import com.fleet.ecocar.ui.EcoCarApp

fun main() = application {
    val state = rememberWindowState(width = 1280.dp, height = 720.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "EcoCar GUI",
        state = state,
    ) {
        EcoCarApp(languageRepository = DesktopLanguageRepository)
    }
}
