package com.fleet.ecocar.infrastructure.navigation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.graphhopper.GHResponse
import com.graphhopper.ResponsePath
import com.graphhopper.util.Instruction
import com.graphhopper.util.InstructionList
import com.graphhopper.util.PointList
import com.graphhopper.util.TranslationMap
import java.util.Locale

/**
 * Converts embedded GraphHopper [GHResponse] into Mapbox Directions API v5 JSON
 * consumable by MapLibre Navigation ([DirectionsRoute.fromJson]).
 *
 * Vendored from GraphHopper's NavigateResponseConverter (Dropwizard-free subset).
 */
object GhNavigateResponseConverter {
    private val mapper = ObjectMapper()
    private val translationMap by lazy { TranslationMap().doImport() }

    fun toDirectionsResponseJson(
        ghResponse: GHResponse,
        locale: Locale = Locale.US,
    ): String {
        require(!ghResponse.hasErrors()) {
            ghResponse.errors.joinToString { it.message.orEmpty() }
        }
        val path = ghResponse.best
        require(path != null) { "No route path in GH response" }

        val root = mapper.createObjectNode()
        root.put("code", "Ok")
        root.putArray("routes").add(routeNode(path, locale))
        val waypoints = root.putArray("waypoints")
        waypoints.add(waypointNode(path.points.getLon(0), path.points.getLat(0)))
        val last = path.points.size() - 1
        waypoints.add(waypointNode(path.points.getLon(last), path.points.getLat(last)))
        return mapper.writeValueAsString(root)
    }

    private fun waypointNode(lon: Double, lat: Double): ObjectNode {
        val node = mapper.createObjectNode()
        node.put("name", "")
        node.putArray("location").add(lon).add(lat)
        return node
    }

    private fun routeNode(path: ResponsePath, locale: Locale): ObjectNode {
        val route = mapper.createObjectNode()
        route.put("geometry", PolylineEncoder.encode(path.points, 5))
        route.put("distance", path.distance)
        route.put("duration", path.time / 1000.0)
        route.put("weight", path.routeWeight)
        route.put("weight_name", "routability")

        val leg = mapper.createObjectNode()
        leg.put("distance", path.distance)
        leg.put("duration", path.time / 1000.0)
        leg.put("weight", path.routeWeight)
        leg.put("summary", "")
        leg.set<ArrayNode>("steps", stepsArray(path, locale))
        route.putArray("legs").add(leg)
        return route
    }

    private fun stepsArray(path: ResponsePath, locale: Locale): ArrayNode {
        val steps = mapper.createArrayNode()
        val instructions: InstructionList = path.instructions
        if (instructions.isEmpty()) {
            steps.add(departStep(path.points, path.distance, path.time / 1000.0))
            return steps
        }
        val translation = translationMap.getWithFallBack(locale)
        for (i in 0 until instructions.size) {
            val instruction = instructions[i]
            val nextDist = if (i + 1 < instructions.size) {
                instructions[i + 1].distance - instruction.distance
            } else {
                path.distance - instruction.distance
            }.coerceAtLeast(1.0)
            val duration = (nextDist / path.distance.coerceAtLeast(1.0)) * (path.time / 1000.0)
            steps.add(instructionStep(instruction, nextDist, duration.coerceAtLeast(1.0), translation))
        }
        return steps
    }

    private fun departStep(points: PointList, distance: Double, duration: Double): ObjectNode {
        val step = mapper.createObjectNode()
        step.put("distance", distance)
        step.put("duration", duration)
        step.put("name", "")
        step.put("mode", "driving")
        step.put("weight", distance)
        step.put("geometry", PolylineEncoder.encode(points, 5))
        step.set<ObjectNode>("maneuver", maneuverNode(points.getLon(0), points.getLat(0), "depart", "Head to destination"))
        return step
    }

    private fun instructionStep(
        instruction: Instruction,
        distance: Double,
        duration: Double,
        translation: com.graphhopper.util.Translation,
    ): ObjectNode {
        val points = instruction.points
        val step = mapper.createObjectNode()
        step.put("distance", distance)
        step.put("duration", duration)
        step.put("name", instruction.name.orEmpty())
        step.put("mode", "driving")
        step.put("weight", distance)
        step.put("geometry", PolylineEncoder.encode(points, 5))
        val lon = points.getLon(0)
        val lat = points.getLat(0)
        step.set<ObjectNode>(
            "maneuver",
            maneuverNode(
                lon,
                lat,
                mapManeuverType(instruction.sign),
                instruction.getTurnDescription(translation),
            ),
        )
        return step
    }

    private fun maneuverNode(lon: Double, lat: Double, type: String, instruction: String): ObjectNode {
        val maneuver = mapper.createObjectNode()
        maneuver.put("type", type)
        maneuver.put("modifier", "")
        maneuver.put("instruction", instruction)
        maneuver.putArray("location").add(lon).add(lat)
        return maneuver
    }

    private fun mapManeuverType(sign: Int): String =
        when (sign) {
            Instruction.REACHED_VIA, Instruction.FINISH -> "arrive"
            Instruction.U_TURN_LEFT, Instruction.U_TURN_RIGHT -> "uturn"
            Instruction.TURN_SLIGHT_LEFT, Instruction.TURN_LEFT, Instruction.TURN_SHARP_LEFT,
            Instruction.TURN_SLIGHT_RIGHT, Instruction.TURN_RIGHT, Instruction.TURN_SHARP_RIGHT,
            -> "turn"
            else -> "continue"
        }
}
