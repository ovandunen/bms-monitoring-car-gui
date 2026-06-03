package com.fleet.ecocar.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.telemetry.EcoBmsTelemetry
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.subnav.EcoSubChipsBar
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.chart_dust_subtitle_bms
import eco_car_gui.composeapp.generated.resources.chart_dust_subtitle_demo
import eco_car_gui.composeapp.generated.resources.chart_dust_title
import eco_car_gui.composeapp.generated.resources.chart_humidity_subtitle_bms
import eco_car_gui.composeapp.generated.resources.chart_humidity_subtitle_demo
import eco_car_gui.composeapp.generated.resources.chart_humidity_title
import eco_car_gui.composeapp.generated.resources.chart_tab_dust
import eco_car_gui.composeapp.generated.resources.chart_tab_humidity
import eco_car_gui.composeapp.generated.resources.chart_tab_temp
import eco_car_gui.composeapp.generated.resources.chart_temp_subtitle_bms
import eco_car_gui.composeapp.generated.resources.chart_temp_subtitle_demo
import eco_car_gui.composeapp.generated.resources.chart_temp_title
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource

private const val HISTORY_LEN = 72
private const val DEMO_TICK_MS = 1_200L

@Composable
fun ChartsSubNav(
    modifier: Modifier = Modifier,
    bmsTelemetry: EcoBmsTelemetry? = null,
) {
    var tab by rememberSaveable { mutableStateOf(0) }

    val tabLabels = listOf(
        stringResource(Res.string.chart_tab_temp),
        stringResource(Res.string.chart_tab_dust),
        stringResource(Res.string.chart_tab_humidity),
    )

    var temperature by remember { mutableStateOf(demoSeries(seed = 11, len = HISTORY_LEN, base = 22f, spread = 6f)) }
    var dustDensity by remember { mutableStateOf(demoSeries(seed = 12, len = HISTORY_LEN, base = 25f, spread = 18f)) }
    var humidity by remember { mutableStateOf(demoSeries(seed = 13, len = HISTORY_LEN, base = 48f, spread = 12f)) }

    LaunchedEffect(bmsTelemetry?.timestamp) {
        val t = bmsTelemetry ?: return@LaunchedEffect
        if (!t.packTemperature.isNaN()) {
            temperature = (temperature.drop(1) + t.packTemperature).takeLast(HISTORY_LEN)
        }
        dustDensity = (dustDensity.drop(1) + t.pm25.toFloat()).takeLast(HISTORY_LEN)
        if (!t.packHumidity.isNaN()) {
            humidity = (humidity.drop(1) + t.packHumidity).takeLast(HISTORY_LEN)
        }
    }

    val latestTelemetry = rememberUpdatedState(bmsTelemetry)
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(DEMO_TICK_MS)
            if (latestTelemetry.value == null) {
                temperature = advanceDemo(temperature, min = 16f, max = 34f, drift = 0.4f)
                dustDensity = advanceDemo(dustDensity, min = 5f, max = 85f, drift = 1.2f)
                humidity = advanceDemo(humidity, min = 28f, max = 78f, drift = 0.55f)
            }
        }
    }

    val bmsOn = bmsTelemetry != null
    Column(modifier = modifier.fillMaxSize()) {
        EcoSubChipsBar(
            labels = tabLabels,
            selectedIndex = tab,
            onSelect = { tab = it },
        )
        HorizontalDivider(color = EcoCarColors.Divider)
        when (tab) {
            0 -> TemperatureChart(
                temperature,
                bmsOn,
                Modifier.weight(1f).fillMaxWidth(),
            )
            1 -> DustDensityChart(
                dustDensity,
                bmsOn,
                Modifier.weight(1f).fillMaxWidth(),
            )
            else -> HumidityChart(
                humidity,
                bmsOn,
                Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TemperatureChart(values: List<Float>, bmsActive: Boolean, modifier: Modifier = Modifier) {
    ChartPanel(
        title = stringResource(Res.string.chart_temp_title),
        subtitle = stringResource(
            if (bmsActive) Res.string.chart_temp_subtitle_bms else Res.string.chart_temp_subtitle_demo,
        ),
        values = values,
        modifier = modifier,
    )
}

@Composable
private fun DustDensityChart(values: List<Float>, bmsActive: Boolean, modifier: Modifier = Modifier) {
    ChartPanel(
        title = stringResource(Res.string.chart_dust_title),
        subtitle = stringResource(
            if (bmsActive) Res.string.chart_dust_subtitle_bms else Res.string.chart_dust_subtitle_demo,
        ),
        values = values,
        modifier = modifier,
    )
}

@Composable
private fun HumidityChart(values: List<Float>, bmsActive: Boolean, modifier: Modifier = Modifier) {
    ChartPanel(
        title = stringResource(Res.string.chart_humidity_title),
        subtitle = stringResource(
            if (bmsActive) Res.string.chart_humidity_subtitle_bms else Res.string.chart_humidity_subtitle_demo,
        ),
        values = values,
        modifier = modifier,
    )
}

@Composable
private fun ChartPanel(
    title: String,
    subtitle: String,
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = EcoCarColors.OnDark,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = EcoCarColors.OnDarkSecondary,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        val safe = values.takeIf { it.size >= 2 } ?: listOf(0f, 0f)
        LineChartCanvas(values = safe, modifier = Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
private fun LineChartCanvas(
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    val lineColor = EcoCarColors.GoldenYellow
    val gridColor = EcoCarColors.Divider
    Canvas(modifier = modifier.padding(8.dp)) {
        val pad = 40f
        val w = size.width - pad * 2
        val h = size.height - pad * 2
        val minV = values.minOrNull() ?: 0f
        val maxV = values.maxOrNull() ?: 1f
        val span = (maxV - minV).coerceAtLeast(0.01f)
        for (i in 0..4) {
            val y = pad + h * i / 4f
            drawLine(gridColor, Offset(pad, y), Offset(pad + w, y), strokeWidth = 1f)
        }
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = pad + w * i / (values.size - 1).coerceAtLeast(1)
            val y = pad + h * (1f - (v - minV) / span)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 3f))
    }
}

private fun demoSeries(seed: Int, len: Int, base: Float, spread: Float): List<Float> {
    val rnd = Random(seed)
    return List(len) { i ->
        val wobble = (rnd.nextFloat() - 0.5f) * spread * 0.15f
        val wave = (spread * sin(i / 6.0)).toFloat()
        (base + wave + wobble).coerceIn(0f, base + spread + 5f)
    }
}

private fun advanceDemo(
    current: List<Float>,
    min: Float,
    max: Float,
    drift: Float,
): List<Float> {
    val rnd = Random.Default
    val last = current.lastOrNull() ?: ((min + max) / 2f)
    val delta = (rnd.nextFloat() - 0.5f) * drift * 2f
    val next = (last + delta).coerceIn(min, max)
    return (current.drop(1) + next).takeLast(HISTORY_LEN)
}
