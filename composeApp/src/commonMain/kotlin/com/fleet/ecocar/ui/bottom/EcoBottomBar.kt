@file:OptIn(ExperimentalFoundationApi::class)

package com.fleet.ecocar.ui.bottom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.theme.socDisplayColor
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.bottom_collapse
import eco_car_gui.composeapp.generated.resources.bottom_co2
import eco_car_gui.composeapp.generated.resources.bottom_co2_kg
import eco_car_gui.composeapp.generated.resources.bottom_expand
import eco_car_gui.composeapp.generated.resources.bottom_info
import eco_car_gui.composeapp.generated.resources.bottom_km
import eco_car_gui.composeapp.generated.resources.bottom_km_dash
import eco_car_gui.composeapp.generated.resources.bottom_range
import eco_car_gui.composeapp.generated.resources.bottom_settings
import eco_car_gui.composeapp.generated.resources.bottom_soc
import eco_car_gui.composeapp.generated.resources.bottom_tons
import eco_car_gui.composeapp.generated.resources.bottom_tons_dash
import eco_car_gui.composeapp.generated.resources.bottom_trip
import eco_car_gui.composeapp.generated.resources.bottom_trip_reset_hint
import java.util.Locale
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.ExperimentalFoundationApi

data class BottomTelemetry(
    val socPercent: Int = 0,
    val tripDistanceKm: Int? = null,
    val rangeKm: Double? = null,
    val co2SavingKg: Double? = null,
)

@Composable
fun EcoBottomBar(
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    telemetry: BottomTelemetry,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onTripLongPress: () -> Unit = {},
    showTripResetHint: Boolean = false,
    onTripResetHintDismissed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val tripText = formatKmChip(telemetry.tripDistanceKm)
    val rangeText = formatKmChip(telemetry.rangeKm?.let { kotlin.math.round(it).toInt() })
    val co2Text = formatCo2Chip(telemetry.co2SavingKg)
    val socColor = telemetry.socPercent.socDisplayColor()
    val rangeDescriptor = telemetry.rangeKm?.let { formatRangeDescriptor(it) }
    val tripResetHint = stringResource(Res.string.bottom_trip_reset_hint)
    val haptic = LocalHapticFeedback.current
    val tripInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(showTripResetHint) {
        if (showTripResetHint) {
            delay(3_000L)
            onTripResetHintDismissed()
        }
    }

    val hintAlpha by animateFloatAsState(
        targetValue = if (showTripResetHint) 1f else 0f,
        label = "tripResetHintAlpha",
    )

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
                    TelemetryChip(
                        label = stringResource(Res.string.bottom_soc),
                        value = "${telemetry.socPercent} %",
                        valueColor = socColor,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(
                            modifier = Modifier
                                .combinedClickable(
                                    interactionSource = tripInteractionSource,
                                    indication = null,
                                    onClick = {},
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onTripLongPress()
                                    },
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(Res.string.bottom_trip),
                                style = MaterialTheme.typography.labelSmall,
                                color = EcoCarColors.OnDarkSecondary,
                            )
                            Text(
                                text = tripText,
                                style = MaterialTheme.typography.titleSmall,
                                color = EcoCarColors.OnDark,
                            )
                        }
                        if (hintAlpha > 0.01f) {
                            Text(
                                text = tripResetHint,
                                style = MaterialTheme.typography.labelSmall,
                                color = EcoCarColors.GoldenYellow.copy(alpha = hintAlpha),
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    TelemetryChip(
                        label = stringResource(Res.string.bottom_range),
                        value = rangeText,
                        valueContentDescription = rangeDescriptor,
                    )
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
                        color = socColor,
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
private fun formatKmChip(km: Int?): String =
    km?.let { stringResource(Res.string.bottom_km, it) }
        ?: stringResource(Res.string.bottom_km_dash)

@Composable
private fun TelemetryChip(
    label: String,
    value: String,
    valueColor: Color = EcoCarColors.OnDark,
    valueContentDescription: String? = null,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = EcoCarColors.OnDarkSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            modifier = if (valueContentDescription != null) {
                Modifier.semantics { contentDescription = valueContentDescription }
            } else {
                Modifier
            },
        )
    }
}

private fun formatRangeDescriptor(rangeKm: Double): String {
    val rounded = kotlin.math.round(rangeKm * 10.0) / 10.0
    return "battery-range=$rounded"
}

@Composable
private fun formatCo2Chip(co2SavingKg: Double?): String {
    if (co2SavingKg == null) return stringResource(Res.string.bottom_tons_dash)
    return if (kotlin.math.abs(co2SavingKg) >= 1000.0) {
        stringResource(Res.string.bottom_tons, formatCo2Decimal(co2SavingKg / 1000.0))
    } else {
        stringResource(Res.string.bottom_co2_kg, formatCo2Decimal(co2SavingKg))
    }
}

/** One decimal; pre-format for Compose Resources (`%1$s` — `%1$.1f` breaks on `$` escaping). */
internal fun formatCo2Decimal(value: Double): String = "%.1f".format(Locale.US, value)
