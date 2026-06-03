package com.fleet.shared.battery.ui.internal

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun StatusHintRow(
    statusHint: String,
    showProgress: Boolean,
    progress: Float?,
    modifier: Modifier = Modifier,
) {
    Text(
        text = statusHint,
        style = MaterialTheme.typography.bodySmall,
        color = BatteryTheme.OnDarkSecondary,
        modifier = modifier,
    )
    if (showProgress && progress != null) {
        LinearProgressIndicator(
            progress = { (progress / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = BatteryTheme.GoldenYellow,
            trackColor = BatteryTheme.Divider,
        )
    }
}
