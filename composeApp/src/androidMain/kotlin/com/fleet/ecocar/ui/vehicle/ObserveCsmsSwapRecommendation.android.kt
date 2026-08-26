package com.fleet.ecocar.ui.vehicle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fleet.ecocar.EcoCarApplication
import com.fleet.ecocar.domain.vehicle.SwapRecommendationEpisodePolicy

@Composable
actual fun ObserveCsmsSwapRecommendation(
    onOptimalSwap: (
        correlationId: String,
        stationId: String,
        stationLabel: String,
        confidencePercent: Int,
    ) -> Unit,
) {
    val app = LocalContext.current.applicationContext as EcoCarApplication
    val recommendation by app.swapRecommendation.collectAsState(initial = null)
    var episodeState by remember { mutableStateOf(SwapRecommendationEpisodePolicy.EpisodeState()) }

    LaunchedEffect(recommendation?.correlationId, recommendation?.stationId) {
        val snapshot = recommendation ?: return@LaunchedEffect
        val (nextState, event) = SwapRecommendationEpisodePolicy.evaluate(
            correlationId = snapshot.correlationId,
            stationId = snapshot.stationId,
            confidence = snapshot.confidence,
            state = episodeState,
        )
        episodeState = nextState
        when (val show = event) {
            is SwapRecommendationEpisodePolicy.Event.ShowRecommendation -> {
                val confidence = (show.recommendation.confidence * 100).toInt().coerceIn(0, 100)
                onOptimalSwap(
                    show.recommendation.correlationId,
                    show.recommendation.stationId,
                    show.recommendation.stationId,
                    confidence,
                )
            }
            null -> Unit
        }
    }
}
