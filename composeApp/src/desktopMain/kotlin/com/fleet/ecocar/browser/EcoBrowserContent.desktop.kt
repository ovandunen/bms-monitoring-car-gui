package com.fleet.ecocar.browser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fleet.ecocar.theme.EcoCarColors

@Composable
actual fun EcoBrowserContent(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "GeckoView-Browser ist nur auf Android verfügbar.",
            style = MaterialTheme.typography.bodyLarge,
            color = EcoCarColors.OnDarkSecondary,
        )
    }
}
