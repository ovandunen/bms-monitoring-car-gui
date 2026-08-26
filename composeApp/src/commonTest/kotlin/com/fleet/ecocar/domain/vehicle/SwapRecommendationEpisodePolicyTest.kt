package com.fleet.ecocar.domain.vehicle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SwapRecommendationEpisodePolicyTest {

    @Test
    fun evaluate_newCorrelationId_emitsShowRecommendation() {
        val (state, event) = SwapRecommendationEpisodePolicy.evaluate(
            correlationId = "rec-001",
            stationId = "Station-A",
            confidence = 0.9,
            state = SwapRecommendationEpisodePolicy.EpisodeState(),
        )

        assertEquals("rec-001", state.lastShownCorrelationId)
        val show = event as SwapRecommendationEpisodePolicy.Event.ShowRecommendation
        assertEquals("Station-A", show.recommendation.stationId)
    }

    @Test
    fun evaluate_sameCorrelationIdTwice_suppressesSecondDialog() {
        var state = SwapRecommendationEpisodePolicy.EpisodeState()
        state = SwapRecommendationEpisodePolicy.evaluate("rec-001", "Station-A", 0.9, state).first
        val (_, event) = SwapRecommendationEpisodePolicy.evaluate("rec-001", "Station-A", 0.9, state)
        assertNull(event)
    }

    @Test
    fun evaluate_newCorrelationIdAfterPrevious_showsAgain() {
        var state = SwapRecommendationEpisodePolicy.EpisodeState()
        state = SwapRecommendationEpisodePolicy.evaluate("rec-001", "Station-A", 0.9, state).first
        val (_, event) = SwapRecommendationEpisodePolicy.evaluate("rec-002", "Station-B", 0.8, state)
        assertEquals(
            SwapRecommendationEpisodePolicy.Event.ShowRecommendation(
                SwapRecommendationEpisodePolicy.Recommendation("rec-002", "Station-B", 0.8),
            ),
            event,
        )
    }

    @Test
    fun evaluate_newCorrelationIdSameStationWithinWindow_suppressesCsmsRestartDuplicate() {
        val t0 = 1_000_000L
        var state = SwapRecommendationEpisodePolicy.EpisodeState()
        state = SwapRecommendationEpisodePolicy.evaluate(
            "rec-001", "Station-A", 0.9, state, nowEpochMs = t0,
        ).first
        val (_, event) = SwapRecommendationEpisodePolicy.evaluate(
            "rec-002", "Station-A", 0.9, state, nowEpochMs = t0 + 60_000L,
        )
        assertNull(event)
    }

    @Test
    fun evaluate_newCorrelationIdSameStationAfterWindow_showsAgain() {
        val t0 = 1_000_000L
        var state = SwapRecommendationEpisodePolicy.EpisodeState()
        state = SwapRecommendationEpisodePolicy.evaluate(
            "rec-001", "Station-A", 0.9, state, nowEpochMs = t0,
        ).first
        val (_, event) = SwapRecommendationEpisodePolicy.evaluate(
            "rec-002", "Station-A", 0.8, state,
            nowEpochMs = t0 + SwapRecommendationEpisodePolicy.SAME_STATION_DEDUP_MS + 1,
        )
        assertEquals("rec-002", (event as SwapRecommendationEpisodePolicy.Event.ShowRecommendation).recommendation.correlationId)
    }

    @Test
    fun evaluate_blankCorrelationId_noEvent() {
        val (_, event) = SwapRecommendationEpisodePolicy.evaluate("", "Station-A", 0.9, SwapRecommendationEpisodePolicy.EpisodeState())
        assertNull(event)
    }
}
