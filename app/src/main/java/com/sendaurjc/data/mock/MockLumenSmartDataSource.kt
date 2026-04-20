package com.sendaurjc.data.mock

import org.osmdroid.util.GeoPoint
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object MockLumenSmartDataSource {
    const val CAMPUS_CENTER_LAT = 40.335
    const val CAMPUS_CENTER_LON = -3.875

    val darkZones = listOf(
        DarkZone(GeoPoint(40.33535, -3.87620), 50.0),
        DarkZone(GeoPoint(40.33445, -3.87495), 50.0)
    )

    fun isUnsafe(point: GeoPoint): Boolean {
        return darkZones.any { zone ->
            distanceMeters(point.latitude, point.longitude, zone.center.latitude, zone.center.longitude) <= zone.radiusMeters
        }
    }

    data class DarkZone(val center: GeoPoint, val radiusMeters: Double)

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val l1 = Math.toRadians(lon1)
        val l2 = Math.toRadians(lon2)
        return acos((sin(p1) * sin(p2)) + (cos(p1) * cos(p2) * cos(l2 - l1))) * r
    }
}
