package com.sendaurjc.domain

import com.sendaurjc.data.mock.MockLumenSmartDataSource
import com.sendaurjc.util.GeoUtils
import org.osmdroid.util.GeoPoint
import java.util.*

class RoutingService {

    fun findSafeRoute(origin: GeoPoint, destination: GeoPoint): List<GeoPoint> {
        val step = 0.00005 // Aprox 5m para mayor precisión
        val openSet = PriorityQueue<Node>(compareBy { it.fScore })
        val closedSet = mutableSetOf<Pair<Int, Int>>()
        val cameFrom = mutableMapOf<Node, Node>()

        val startNode = Node(origin, 0.0, GeoUtils.distanceMeters(origin, destination))
        openSet.add(startNode)

        var iterations = 0
        val maxIterations = 8000 // Aumentado para el paso más pequeño
        var bestNode: Node? = null
        var minH = Double.MAX_VALUE

        while (openSet.isNotEmpty() && iterations < maxIterations) {
            iterations++
            val current = openSet.poll() ?: break

            val h = GeoUtils.distanceMeters(current.point, destination)
            if (h < minH) {
                minH = h
                bestNode = current
            }

            // Si estamos muy cerca del destino, terminamos
            if (h < 15.0) {
                return reconstructPath(cameFrom, current, origin, destination)
            }

            closedSet.add(gridKey(current.point, step))

            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue

                    val nextPoint = GeoPoint(
                        current.point.latitude + dx * step,
                        current.point.longitude + dy * step
                    )

                    if (closedSet.contains(gridKey(nextPoint, step))) continue

                    // Verificamos el punto destino y el punto medio para evitar atravesar esquinas
                    val midPoint = GeoUtils.interpolate(current.point, nextPoint, 0.5)

                    if (MockLumenSmartDataSource.isUnsafe(nextPoint) || MockLumenSmartDataSource.isUnsafe(midPoint)) continue
                    if (MockLumenSmartDataSource.isInsideBuilding(nextPoint) || MockLumenSmartDataSource.isInsideBuilding(midPoint)) continue
                    if (MockLumenSmartDataSource.isInsideGreenZone(nextPoint) || MockLumenSmartDataSource.isInsideGreenZone(midPoint)) continue

                    val tentativeGScore = current.gScore + GeoUtils.distanceMeters(current.point, nextPoint)
                    val neighbor = Node(nextPoint, tentativeGScore, GeoUtils.distanceMeters(nextPoint, destination))

                    val existing = openSet.find { it.gridKey(step) == neighbor.gridKey(step) }
                    if (existing == null || tentativeGScore < existing.gScore) {
                        cameFrom[neighbor] = current
                        if (existing != null) openSet.remove(existing)
                        openSet.add(neighbor)
                    }
                }
            }
        }

        return reconstructPath(cameFrom, bestNode ?: startNode, origin, destination)
    }

    private fun gridKey(point: GeoPoint, step: Double): Pair<Int, Int> {
        return (point.latitude / step).toInt() to (point.longitude / step).toInt()
    }

    private fun reconstructPath(cameFrom: Map<Node, Node>, lastNode: Node, origin: GeoPoint, destination: GeoPoint): List<GeoPoint> {
        val path = mutableListOf<GeoPoint>()
        var curr: Node? = lastNode
        while (curr != null) {
            path.add(0, curr.point)
            curr = cameFrom[curr]
        }
        
        // ASEGURAMOS ORIGEN Y DESTINO EXACTOS
        val finalPath = path.toMutableList()
        if (finalPath.isNotEmpty()) {
            finalPath[0] = origin
            if (GeoUtils.distanceMeters(finalPath.last(), destination) < 50.0) {
                finalPath[finalPath.lastIndex] = destination
            } else {
                finalPath.add(destination)
            }
        } else {
            return listOf(origin, destination)
        }
        return finalPath
    }

    private data class Node(val point: GeoPoint, val gScore: Double, val hScore: Double) {
        val fScore: Double get() = gScore + hScore
        fun gridKey(step: Double) = (point.latitude / step).toInt() to (point.longitude / step).toInt()
    }
}
