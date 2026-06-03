package com.fleet.ecocar.ui.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.subnav.EcoSubChipsBar
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.alerts_empty
import eco_car_gui.composeapp.generated.resources.alerts_title
import eco_car_gui.composeapp.generated.resources.battery_demo_hint
import eco_car_gui.composeapp.generated.resources.battery_live_hint
import eco_car_gui.composeapp.generated.resources.battery_sniffer_btn
import eco_car_gui.composeapp.generated.resources.battery_tab_alerts
import eco_car_gui.composeapp.generated.resources.battery_tab_cells
import eco_car_gui.composeapp.generated.resources.battery_tab_overview
import eco_car_gui.composeapp.generated.resources.battery_title
import eco_car_gui.composeapp.generated.resources.cells_bms_title
import eco_car_gui.composeapp.generated.resources.cells_demo_hint
import eco_car_gui.composeapp.generated.resources.cells_demo_title
import eco_car_gui.composeapp.generated.resources.cells_live_hint
import eco_car_gui.composeapp.generated.resources.metric_pack_current
import eco_car_gui.composeapp.generated.resources.metric_pack_voltage
import eco_car_gui.composeapp.generated.resources.metric_power
import eco_car_gui.composeapp.generated.resources.metric_soc
import eco_car_gui.composeapp.generated.resources.severity_critical
import eco_car_gui.composeapp.generated.resources.severity_info
import eco_car_gui.composeapp.generated.resources.severity_warning
import org.jetbrains.compose.resources.stringResource

@Composable
fun BatterySubNav(
    onOpenSniffer: () -> Unit,
    ecoBmsTelemetry: EcoBmsTelemetry? = null,
    modifier: Modifier = Modifier,
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    val bmsActive = ecoBmsTelemetry != null && ecoBmsTelemetry.timestamp > 0L
    val cellVolts = ecoBmsTelemetry?.cellVolts.orEmpty()

    val tabLabels = listOf(
        stringResource(Res.string.battery_tab_overview),
        stringResource(Res.string.battery_tab_cells),
        stringResource(Res.string.battery_tab_alerts),
    )

    Column(modifier = modifier.fillMaxSize()) {
        EcoSubChipsBar(
            labels = tabLabels,
            selectedIndex = tab,
            onSelect = { tab = it },
        )
        HorizontalDivider(color = EcoCarColors.Divider)
        when (tab) {
            0 -> BatteryOverviewContent(
                onOpenSniffer = onOpenSniffer,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            1 -> BatteryCellsGrid(
                cellVolts = cellVolts,
                bmsActive = bmsActive,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            else -> BatteryAlertsList(
                alerts = emptyList(),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun BatteryOverviewTab(
    snapshot: DemoBatterySnapshot,
    bmsActive: Boolean,
    onOpenSniffer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val socIsLow = snapshot.socPercent < 30f
    val socColor = if (socIsLow) MaterialTheme.colorScheme.error else EcoCarColors.GoldenYellow
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.battery_title),
            style = MaterialTheme.typography.titleLarge,
            color = EcoCarColors.OnDark,
        )
        Text(
            text = stringResource(
                if (bmsActive) Res.string.battery_live_hint else Res.string.battery_demo_hint,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = EcoCarColors.OnDarkSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                title = stringResource(Res.string.metric_soc),
                value = "%.1f".format(snapshot.socPercent),
                unit = "%",
                valueColor = socColor,
                unitColor = if (socIsLow) socColor else EcoCarColors.OnDark,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = stringResource(Res.string.metric_pack_voltage),
                value = "%.1f".format(snapshot.packVoltageV),
                unit = "V",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                title = stringResource(Res.string.metric_pack_current),
                value = "%.1f".format(snapshot.packCurrentA),
                unit = "A",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                title = stringResource(Res.string.metric_power),
                value = "%.2f".format(snapshot.powerKw),
                unit = "kW",
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            onClick = onOpenSniffer,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoCarColors.GoldenYellow,
                contentColor = EcoCarColors.NearBlack,
            ),
        ) {
            Text(stringResource(Res.string.battery_sniffer_btn))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    unit: String,
    valueColor: Color = EcoCarColors.GoldenYellow,
    unitColor: Color = EcoCarColors.OnDark,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = EcoCarColors.SurfaceElevated),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = EcoCarColors.OnDarkSecondary,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                )
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = unitColor,
                )
            }
        }
    }
}

@Composable
private fun BatteryCellsGrid(
    cellVolts: List<Float>,
    bmsActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(12.dp)) {
        Text(
            text = stringResource(
                if (bmsActive) {
                    Res.string.cells_bms_title
                } else {
                    Res.string.cells_demo_title
                },
                cellVolts.size,
            ),
            style = MaterialTheme.typography.titleMedium,
            color = EcoCarColors.OnDark,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            text = stringResource(
                if (bmsActive) Res.string.cells_live_hint else Res.string.cells_demo_hint,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = EcoCarColors.OnDarkSecondary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(cellVolts) { index, v ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = EcoCarColors.SurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(
                        Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EcoCarColors.OnDarkSecondary,
                        )
                        Text(
                            text = "%.2f V".format(v),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = EcoCarColors.GoldenYellow,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryAlertsList(
    alerts: List<DemoBatteryAlert>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = stringResource(Res.string.alerts_title),
                style = MaterialTheme.typography.titleMedium,
                color = EcoCarColors.OnDark,
            )
        }
        if (alerts.isEmpty()) {
            item {
                Text(
                    text = stringResource(Res.string.alerts_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EcoCarColors.OnDarkSecondary,
                )
            }
        } else {
            items(alerts, key = { it.message }) { alert ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = EcoCarColors.SurfaceElevated),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = severityLabel(alert.severity),
                            color = EcoCarColors.GoldenYellow,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = alert.message,
                            color = EcoCarColors.OnDark,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun severityLabel(severity: DemoAlertSeverity): String = stringResource(
    when (severity) {
        DemoAlertSeverity.INFO -> Res.string.severity_info
        DemoAlertSeverity.WARNING -> Res.string.severity_warning
        DemoAlertSeverity.CRITICAL -> Res.string.severity_critical
    },
)
