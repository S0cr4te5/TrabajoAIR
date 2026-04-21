package com.sendaurjc.domain

import com.sendaurjc.data.mock.MockLumenSmartDataSource
import com.sendaurjc.util.GeoUtils
import org.osmdroid.util.GeoPoint
import java.util.*

class RoutingService {

    fun findSafeRoute(origin: GeoPoint, destination: GeoPoint): List<GeoPoint> {
        val step = 0.0002 // ~20m - más rápido
        val openSet = PriorityQueue<Node>(compareBy { it.fScore })
        val gScoreMap = mutableMapOf<String, Double>()
        val closedSet = mutableSetOf<String>()
        val nodeMap = mutableMapOf<String, Node>() // Mapa de nodos para reconstrucción

        val startNode = Node(origin, 0.0, GeoUtils.distanceMeters(origin, destination), null)
        val startKey = gridKeyString(origin, step)
        openSet.add(startNode)
        gScoreMap[startKey] = 0.0
        nodeMap[startKey] = startNode

        var iterations = 0
        val maxIterations = 500 // Aún más reducido
        var bestNode: Node? = startNode
        var minH = GeoUtils.distanceMeters(origin, destination)

        while (openSet.isNotEmpty() && iterations < maxIterations) {
            iterations++
            val current = openSet.poll() ?: break
            val currentKey = gridKeyString(current.point, step)

            if (closedSet.contains(currentKey)) continue
            closedSet.add(currentKey)

            val h = GeoUtils.distanceMeters(current.point, destination)
            if (h < minH) {
                minH = h
                bestNode = current
            }

            // Cercano al destino
            if (h < 20.0) {
                return reconstructPath(bestNode, origin, destination)
            }

            // Solo 4 direcciones
            val neighbors = listOf(
                GeoPoint(current.point.latitude + step, current.point.longitude),
                GeoPoint(current.point.latitude - step, current.point.longitude),
                GeoPoint(current.point.latitude, current.point.longitude + step),
                GeoPoint(current.point.latitude, current.point.longitude - step)
            )

            for (nextPoint in neighbors) {
                val nextKey = gridKeyString(nextPoint, step)
                if (closedSet.contains(nextKey)) continue
                if (!isPointSafe(nextPoint)) continue

                val tentativeGScore = current.gScore + GeoUtils.distanceMeters(current.point, nextPoint)
                val previousGScore = gScoreMap[nextKey] ?: Double.MAX_VALUE

                if (tentativeGScore < previousGScore) {
                    gScoreMap[nextKey] = tentativeGScore
                    val hScore = GeoUtils.distanceMeters(nextPoint, destination)
                    val neighbor = Node(nextPoint, tentativeGScore, hScore, current)
                    nodeMap[nextKey] = neighbor
                    openSet.add(neighbor)
                }
            }
        }

        return reconstructPath(bestNode, origin, destination)
    }

    private fun isPointSafe(point: GeoPoint): Boolean {
        return !MockLumenSmartDataSource.isUnsafe(point) &&
               !MockLumenSmartDataSource.isInsideBuilding(point) &&
               !MockLumenSmartDataSource.isInsideGreenZone(point)
    }

    private fun gridKeyString(point: GeoPoint, step: Double): String {
        val lat = (point.latitude / step).toInt()
        val lon = (point.longitude / step).toInt()
        return "$lat,$lon"
    }

    private fun reconstructPath(node: Node?, origin: GeoPoint, destination: GeoPoint): List<GeoPoint> {
        if (node == null) return listOf(origin, destination)

        val path = mutableListOf<GeoPoint>()
        var current: Node? = node
        val maxSteps = 10000 // Prevenir loops infinitos

        var steps = 0
        while (current != null && steps < maxSteps) {
            path.add(0, current.point)
            current = current.parent
            steps++
        }

        if (path.isEmpty()) {
            return listOf(origin, destination)
        }

        val finalPath = path.toMutableList()
        finalPath[0] = origin
        if (GeoUtils.distanceMeters(finalPath.last(), destination) < 50.0) {
            finalPath[finalPath.lastIndex] = destination
        } else {
            finalPath.add(destination)
        }
        return finalPath
    }

    private data class Node(
        val point: GeoPoint,
        val gScore: Double,
        val hScore: Double,
        val parent: Node? = null
    ) {
        val fScore: Double get() = gScore + hScore
    }
}
