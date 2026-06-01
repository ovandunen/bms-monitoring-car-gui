package com.ecocar.gui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.StateFlow

@Composable
fun StateFlow<AppLanguage>.collectLanguageAsState(
    initial: AppLanguage = AppLanguage.DE,
): State<AppLanguage> {
    val state = remember { mutableStateOf(initial) }
    androidx.compose.runtime.LaunchedEffect(this) {
        collect { state.value = it }
    }
    return state
}
