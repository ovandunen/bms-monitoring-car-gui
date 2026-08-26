package com.fleet.ecocar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberAcceptedSwapNavigationHandler(): (String) -> Unit =
    remember { { _ -> } }
