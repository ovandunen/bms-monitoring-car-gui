package com.fleet.ecocar.domain.vehicle

/**
 * Stufe 1 (CSMS push): one dialog per recommendation [correlationId].
 * Suppresses repeat prompts on MQTT retain/reconnect for the same id.
 *
 * Also suppresses a **new** correlationId for the **same station** within [SAME_STATION_DEDUP_MS]
 * (CSMS restart issues fresh UUIDs). Intentionally aligned with CSMS `validUntil`: while a
 * recommendation for that station is still within its validity window, the driver should not
 * get a second dialog for the same advice — including legitimate re-evaluation with a new id.
 */
object SwapRecommendationEpisodePolicy {

    /** Matches CSMS default recommendation validity window (15 min). */
    const val SAME_STATION_DEDUP_MS = 15 * 60 * 1000L

    data class EpisodeState(
        val lastShownCorrelationId: String? = null,
        val lastShownStationId: String? = null,
        val lastShownAtEpochMs: Long = 0L,
    )

    data class Recommendation(
        val correlationId: String,
        val stationId: String,
        val confidence: Double,
    )

    sealed interface Event {
        data class ShowRecommendation(val recommendation: Recommendation) : Event
    }

    fun evaluate(
        correlationId: String?,
        stationId: String?,
        confidence: Double,
        state: EpisodeState,
        nowEpochMs: Long = 0L,
    ): Pair<EpisodeState, Event?> {
        val corr = correlationId?.takeIf { it.isNotBlank() } ?: return state to null
        val station = stationId?.takeIf { it.isNotBlank() } ?: return state to null
        if (corr == state.lastShownCorrelationId) return state to null
        val now = if (nowEpochMs > 0L) nowEpochMs else System.currentTimeMillis()
        if (
            station == state.lastShownStationId &&
            state.lastShownAtEpochMs > 0L &&
            now - state.lastShownAtEpochMs < SAME_STATION_DEDUP_MS
        ) {
            return state to null
        }
        val recommendation = Recommendation(corr, station, confidence)
        return state.copy(
            lastShownCorrelationId = corr,
            lastShownStationId = station,
            lastShownAtEpochMs = now,
        ) to Event.ShowRecommendation(recommendation)
    }
}
