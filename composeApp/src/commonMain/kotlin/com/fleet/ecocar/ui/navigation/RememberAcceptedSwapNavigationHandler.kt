package com.fleet.ecocar.ui.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun rememberAcceptedSwapNavigationHandler(): (String) -> Unit
