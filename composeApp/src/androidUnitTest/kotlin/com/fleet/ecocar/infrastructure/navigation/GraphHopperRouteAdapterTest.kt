package com.fleet.ecocar.infrastructure.navigation

import com.fleet.ecocar.domain.navigation.LatLon
import com.graphhopper.GraphHopper
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class GraphHopperRouteAdapterTest {

    @Test
    fun route_computedFromTinySyntheticGraph_withoutNetwork() {
        val tempRoot = createTempDir(prefix = "gh-route-test").apply { deleteOnExit() }
        val osmFile = File(tempRoot, "tiny.osm")
        osmFile.writeText(TINY_OSM)

        val graphDir = File(tempRoot, "graph")
        val hopper = GraphHopper()
        hopper.init(graphHopperCarConfig())
        hopper.setOSMFile(osmFile.absolutePath)
        hopper.setGraphHopperLocation(graphDir.absolutePath)
        hopper.importOrLoad()

        val response = hopper.route(
            com.graphhopper.GHRequest(14.7167, -17.4677, 14.7175, -17.4660)
                .setProfile("car"),
        )
        assertTrue(!response.hasErrors())

        val json = GhNavigateResponseConverter.toDirectionsResponseJson(response)
        assertTrue(json.contains("\"routes\""))
        assertTrue(json.contains("\"geometry\""))
    }

    @Test
    fun routeProvider_latLonPair_producesDirectionsJson() {
        val tempRoot = createTempDir(prefix = "gh-provider-test").apply { deleteOnExit() }
        val osmFile = File(tempRoot, "tiny.osm")
        osmFile.writeText(TINY_OSM)
        val graphDir = File(tempRoot, "graph")

        val hopper = GraphHopper()
        hopper.init(graphHopperCarConfig())
        hopper.setOSMFile(osmFile.absolutePath)
        hopper.setGraphHopperLocation(graphDir.absolutePath)
        hopper.importOrLoad()

        val result = hopper.route(
            com.graphhopper.GHRequest(
                LatLon(14.7167, -17.4677).latitude,
                LatLon(14.7167, -17.4677).longitude,
                LatLon(14.7175, -17.4660).latitude,
                LatLon(14.7175, -17.4660).longitude,
            ).setProfile("car"),
        )
        val json = GhNavigateResponseConverter.toDirectionsResponseJson(result)
        assertTrue(json.contains("duration"))
    }

    private companion object {
        val TINY_OSM = """
            <?xml version="1.0" encoding="UTF-8"?>
            <osm version="0.6">
              <node id="1" lat="14.7167" lon="-17.4677"/>
              <node id="2" lat="14.7170" lon="-17.4670"/>
              <node id="3" lat="14.7175" lon="-17.4660"/>
              <way id="10">
                <nd ref="1"/><nd ref="2"/>
                <tag k="highway" v="primary"/>
              </way>
              <way id="11">
                <nd ref="2"/><nd ref="3"/>
                <tag k="highway" v="primary"/>
              </way>
            </osm>
        """.trimIndent()
    }
}
