package com.fleet.ecocar.domain.vehicle

/**
 * Which station to highlight on the map depends on escalation source (Meldungskonzept).
 */
object ChargingStationSelectionPolicy {

    enum class Mode {
        /** Stufe 1 — CSMS advises swap: station with the fullest charged slot. */
        CsmsAdvisedFullest,

        /** Stufe 2/3 — SOC-driven: nearest station (any). */
        NearestAny,
    }

    fun mode(csmsAdvised: Boolean): Mode =
        if (csmsAdvised) Mode.CsmsAdvisedFullest else Mode.NearestAny
}
