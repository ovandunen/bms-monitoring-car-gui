package com.fleet.ecocar.di

import com.fleet.ecocar.domain.navigation.LatLon
import com.fleet.ecocar.domain.navigation.RouteProvider

/**
 * Returns pre-baked Mapbox Directions v5 JSON — never hits GraphHopper.
 */
class FakeRouteProvider : RouteProvider {

    var directionsJson: String = NavigationTestFixtures.MOCK_DIRECTIONS_JSON

    override suspend fun route(origin: LatLon, destination: LatLon): Result<String> =
        Result.success(directionsJson)
}

/** Shared fixture constants for navigation UI tests. */
object NavigationTestFixtures {
    const val LAT_START = 14.7167
    const val LNG_START = -17.4677
    const val LAT_TURN = 14.7175
    const val LNG_TURN = -17.4660

    /** Fixture CSMS-recommended station (Dakar corridor placeholder). */
    const val LAT_STATION = 14.7175
    const val LNG_STATION = -17.4660
    const val FIXTURE_STATION_ID = "fixture-station-dakar-01"
    const val STATION_ARRIVAL_RADIUS_M = 15.0

    val MOCK_DIRECTIONS_JSON: String = """
        {
          "code": "Ok",
          "routes": [{
            "geometry": "wpfa[frci`@_q@giB",
            "distance": 200.0,
            "duration": 60.0,
            "weight": 60.0,
            "weight_name": "routability",
            "legs": [{
              "distance": 200.0,
              "duration": 60.0,
              "weight": 60.0,
              "summary": "",
              "steps": [
                {
                  "distance": 185.0,
                  "duration": 55.0,
                  "name": "",
                  "mode": "driving",
                  "weight": 55.0,
                  "geometry": "wpfa[frci`@_q@giB",
                  "intersections": [
                    {
                      "location": [-17.4677, 14.7167],
                      "bearings": [0],
                      "entry": [true],
                      "out": 0
                    }
                  ],
                  "maneuver": {
                    "type": "depart",
                    "instruction": "Head north",
                    "bearing_before": 0,
                    "bearing_after": 0,
                    "location": [-17.4677, 14.7167]
                  }
                },
                {
                  "distance": 15.0,
                  "duration": 5.0,
                  "name": "",
                  "mode": "driving",
                  "weight": 5.0,
                  "geometry": "wbha[~g`i`@?_q@",
                  "intersections": [
                    {
                      "location": [-17.4660, 14.7175],
                      "bearings": [0, 90],
                      "entry": [false, true],
                      "in": 0,
                      "out": 1
                    }
                  ],
                  "maneuver": {
                    "type": "turn",
                    "modifier": "right",
                    "instruction": "Turn right",
                    "bearing_before": 0,
                    "bearing_after": 90,
                    "location": [-17.4660, 14.7175]
                  }
                }
              ]
            }]
          }],
          "waypoints": [
            {"name": "", "location": [-17.4677, 14.7167]},
            {"name": "", "location": [-17.4660, 14.7175]}
          ]
        }
    """.trimIndent()
}
