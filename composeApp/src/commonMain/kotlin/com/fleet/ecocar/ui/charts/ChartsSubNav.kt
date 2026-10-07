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
import eco_car_gui.composeapp.generated.resources.chart_dust_title
import eco_car_gui.composeapp.generated.resources.chart_humidity_subtitle_bms
import eco_car_gui.composeapp.generated.resources.chart_humidity_title
import eco_car_gui.composeapp.generated.resources.chart_no_sensor_data
import eco_car_gui.composeapp.generated.resources.chart_tab_dust
import eco_car_gui.composeapp.generated.resources.chart_tab_humidity
import eco_car_gui.composeapp.generated.resources.chart_tab_temp
import eco_car_gui.composeapp.generated.resources.chart_temp_subtitle_bms
import eco_car_gui.composeapp.generated.resources.chart_temp_title
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource

private const val HISTORY_LEN = 72
private val SENSOR_CHART_STALE = 30.seconds

@Composable
fun ChartsSubNav(
    modifier: Modifier = Modifier,
    bmsTelemetry: EcoBmsTelemetry? = null,
    timeSource: TimeSource = TimeSource.Monotonic,
) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var series by remember { mutableStateOf(SensorChartSeries()) }
    var tick by remember { mutableStateOf(0) }

    val tabLabels = listOf(
        stringResource(Res.string.chart_tab_temp),
        stringResource(Res.string.chart_tab_dust),
        stringResource(Res.string.chart_tab_humidity),
    )

    LaunchedEffect(bmsTelemetry) {
        val reading = bmsTelemetry ?: return@LaunchedEffect
        series = series.withReading(reading, timeSource)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1.seconds)
            tick++
        }
    }

    val live = remember(series, tick) { series.showsLiveChart() }
    Column(modifier = modifier.fillMaxSize()) {
        EcoSubChipsBar(
            labels = tabLabels,
            selectedIndex = tab,
            onSelect = { tab = it },
        )
        HorizontalDivider(color = EcoCarColors.Divider)
        when (tab) {
            0 -> SensorChart(
                title = stringResource(Res.string.chart_temp_title),
                subtitle = stringResource(Res.string.chart_temp_subtitle_bms),
                values = if (live) series.temperature else null,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            1 -> SensorChart(
                title = stringResource(Res.string.chart_dust_title),
                subtitle = stringResource(Res.string.chart_dust_subtitle_bms),
                values = if (live) series.pm25 else null,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            else -> SensorChart(
                title = stringResource(Res.string.chart_humidity_title),
                subtitle = stringResource(Res.string.chart_humidity_subtitle_bms),
                values = if (live) series.humidity else null,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SensorChart(
    title: String,
    subtitle: String,
    values: List<Float>?,
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
        if (values == null) {
            Text(
                text = stringResource(Res.string.chart_no_sensor_data),
                style = MaterialTheme.typography.bodySmall,
                color = EcoCarColors.OnDarkSecondary,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        } else {
            LineChartCanvas(values = values, modifier = Modifier.weight(1f).fillMaxWidth())
        }
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
        if (values.isEmpty()) return@Canvas
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
        fun yOf(v: Float) = pad + h * (1f - (v - minV) / span)
        if (values.size == 1) {
            drawCircle(lineColor, radius = 4f, center = Offset(pad, yOf(values[0])))
            return@Canvas
        }
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = pad + w * i / (values.size - 1)
            val y = yOf(v)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 3f))
    }
}

internal data class SensorChartSeries(
    val temperature: List<Float> = emptyList(),
    val humidity: List<Float> = emptyList(),
    val pm25: List<Float> = emptyList(),
    val receivedAt: TimeMark? = null,
) {
    fun withReading(telemetry: EcoBmsTelemetry, timeSource: TimeSource): SensorChartSeries {
        val sample = liveChartSample(telemetry)
        return copy(
            temperature = append(temperature, sample.temperatureC),
            humidity = append(humidity, sample.humidity),
            pm25 = (pm25 + sample.pm25).takeLast(HISTORY_LEN),
            receivedAt = timeSource.markNow(),
        )
    }

    fun showsLiveChart(): Boolean {
        val received = receivedAt ?: return false
        return received.elapsedNow() <= SENSOR_CHART_STALE
    }

    private fun append(current: List<Float>, value: Float): List<Float> =
        if (value.isNaN()) current else (current + value).takeLast(HISTORY_LEN)
}

internal data class LiveChartSample(
    val temperatureC: Float,
    val humidity: Float,
    val pm25: Float,
)

internal fun liveChartSample(telemetry: EcoBmsTelemetry): LiveChartSample = LiveChartSample(
    temperatureC = telemetry.ambientTemperatureC,
    humidity = telemetry.humidity,
    pm25 = telemetry.pm25.toFloat(),
)
