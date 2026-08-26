package com.fleet.ecocar.infrastructure.navigation

import com.graphhopper.GraphHopperConfig
import com.graphhopper.config.CHProfile
import com.graphhopper.config.Profile
import com.graphhopper.util.CustomModel

internal fun graphHopperCarConfig(): GraphHopperConfig =
    GraphHopperConfig().apply {
        setProfiles(listOf(Profile("car").setCustomModel(CustomModel())))
        setCHProfiles(listOf(CHProfile("car")))
    }
