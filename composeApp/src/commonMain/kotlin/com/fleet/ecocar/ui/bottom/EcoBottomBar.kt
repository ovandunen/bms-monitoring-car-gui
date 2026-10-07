package com.fleet.ecocar.ui.bottom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.domain.vehicle.configuredLowBatteryPercent
import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.theme.socDisplayColor
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.battery_cloud_offline
import eco_car_gui.composeapp.generated.resources.battery_no_data
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
import java.util.Locale
import org.jetbrains.compose.resources.stringResource

data class BottomTelemetry(
    val socPercent: Int = 0,
    val tripDistanceKm: Int? = null,
    val rangeKm: Double? = null,
    val co2SavingKg: Double? = null,
    val batteryDataStale: Boolean = false,
    val cloudConnected: Boolean = true,
    val hasLiveData: Boolean = false,
    val sensorError: Boolean = false,
    val ds18b20Error: Boolean = false,
    val sht31Error: Boolean = false,
    val pms5003Error: Boolean = false,
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
    val tripText = formatKmChip(telemetry.tripDistanceKm)
    val rangeText = formatKmChip(telemetry.rangeKm?.let { kotlin.math.round(it).toInt() })
    val co2Text = formatCo2Chip(telemetry.co2SavingKg)
    val socColor = telemetry.socPercent.socDisplayColor(
        configuredLowBatteryPercent(),
        telemetry.hasLiveData,
    )
    val rangeDescriptor = telemetry.rangeKm?.let { formatRangeDescriptor(it) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Column {
            HorizontalDivider(color = EcoCarColors.Divider, thickness = 1.dp)
            val chips = bottomStatusChipLabels(
                batteryDataStale = telemetry.batteryDataStale,
                cloudConnected = telemetry.cloudConnected,
                sensorError = telemetry.sensorError,
                ds18b20Error = telemetry.ds18b20Error,
                sht31Error = telemetry.sht31Error,
                pms5003Error = telemetry.pms5003Error,
                noBatteryData = stringResource(Res.string.battery_no_data),
                cloudOffline = stringResource(Res.string.battery_cloud_offline),
            )
            if (chips.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    chips.forEach { label -> BottomStatusChip(label) }
                }
            }
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
                    TelemetryChip(
                        label = stringResource(Res.string.bottom_trip),
                        value = tripText,
                    )
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

internal val BottomStatusChipColor = EcoCarColors.LowSocOrange

internal fun sensorErrorChipText(
    sensorError: Boolean,
    ds18b20Error: Boolean,
    sht31Error: Boolean,
    pms5003Error: Boolean,
): String? {
    val names = buildList {
        if (ds18b20Error) add("Outside temperature (DS18B20)")
        if (sht31Error) add("Humidity (SHT31)")
        if (pms5003Error) add("PMS5003")
    }
    return when {
        names.isNotEmpty() -> "Sensor error: ${names.joinToString(", ")}"
        sensorError -> "Sensor error: outside sensors"
        else -> null
    }
}

internal fun bottomStatusChipLabels(
    batteryDataStale: Boolean,
    cloudConnected: Boolean,
    sensorError: Boolean,
    ds18b20Error: Boolean,
    sht31Error: Boolean,
    pms5003Error: Boolean,
    noBatteryData: String,
    cloudOffline: String,
): List<String> = buildList {
    if (batteryDataStale) add(noBatteryData)
    if (!cloudConnected) add(cloudOffline)
    sensorErrorChipText(sensorError, ds18b20Error, sht31Error, pms5003Error)?.let { add(it) }
}

internal fun BottomTelemetry.withSensorErrors(telemetry: EcoBmsTelemetry?): BottomTelemetry =
    copy(
        sensorError = telemetry?.sensorError == true,
        ds18b20Error = telemetry?.ds18b20Error == true,
        sht31Error = telemetry?.sht31Error == true,
        pms5003Error = telemetry?.pms5003Error == true,
    )

@Composable
private fun BottomStatusChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        border = BorderStroke(2.dp, BottomStatusChipColor),
    ) {
        Text(
            text = text,
            color = BottomStatusChipColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
        )
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
