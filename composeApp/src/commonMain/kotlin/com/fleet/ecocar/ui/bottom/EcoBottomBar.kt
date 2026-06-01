package com.fleet.ecocar.ui.bottom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.bottom_collapse
import eco_car_gui.composeapp.generated.resources.bottom_co2
import eco_car_gui.composeapp.generated.resources.bottom_expand
import eco_car_gui.composeapp.generated.resources.bottom_info
import eco_car_gui.composeapp.generated.resources.bottom_km
import eco_car_gui.composeapp.generated.resources.bottom_range
import eco_car_gui.composeapp.generated.resources.bottom_settings
import eco_car_gui.composeapp.generated.resources.bottom_soc
import eco_car_gui.composeapp.generated.resources.bottom_tons
import eco_car_gui.composeapp.generated.resources.bottom_trip
import org.jetbrains.compose.resources.stringResource

data class BottomTelemetry(
    val socPercent: Int = 74,
    val tripDistanceKm: Int = 71,
    val rangeKm: Int = 103,
    val co2SavingTons: Double = -1.7,
)

@Composable
fun EcoBottomBar(
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    telemetry: BottomTelemetry,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tripText = stringResource(Res.string.bottom_km, telemetry.tripDistanceKm)
    val rangeText = stringResource(Res.string.bottom_km, telemetry.rangeKm)
    val co2Text = stringResource(Res.string.bottom_tons, telemetry.co2SavingTons)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = EcoCarColors.Divider, thickness = 1.dp)
            if (expanded) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TelemetryChip(stringResource(Res.string.bottom_soc), "${telemetry.socPercent} %")
                    TelemetryChip(stringResource(Res.string.bottom_trip), tripText)
                    TelemetryChip(stringResource(Res.string.bottom_range), rangeText)
                    TelemetryChip(stringResource(Res.string.bottom_co2), co2Text)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = null,
                            tint = EcoCarColors.GoldenYellow,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text(stringResource(Res.string.bottom_settings), color = EcoCarColors.OnDark)
                    }
                    TextButton(onClick = onInfoClick) {
                        Text(stringResource(Res.string.bottom_info), color = EcoCarColors.OnDark)
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = EcoCarColors.GoldenYellow,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${telemetry.socPercent} %",
                        style = MaterialTheme.typography.titleMedium,
                        color = EcoCarColors.GoldenYellow,
                    )
                    Text(
                        text = "$tripText · $rangeText · $co2Text",
                        style = MaterialTheme.typography.bodySmall,
                        color = EcoCarColors.OnDarkSecondary,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                        contentDescription = stringResource(
                            if (expanded) Res.string.bottom_collapse else Res.string.bottom_expand,
                        ),
                        tint = EcoCarColors.GoldenYellow,
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = EcoCarColors.OnDarkSecondary)
        Text(text = value, style = MaterialTheme.typography.titleSmall, color = EcoCarColors.OnDark)
    }
}
