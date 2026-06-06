package com.fleet.ecocar.ui.bottom

interface TripResetHintRepository {
    suspend fun isHintShown(): Boolean
    suspend fun markHintShown()
}
