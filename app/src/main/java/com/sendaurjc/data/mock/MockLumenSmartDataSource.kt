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

    val buildings = listOf(
        Building(GeoPoint(40.337083, -3.875583), 35.0), // Biblioteca
        Building(GeoPoint(40.335802, -3.876332), 30.0), // Aulario 1
        Building(GeoPoint(40.336420, -3.877150), 30.0), // Aulario 2
        Building(GeoPoint(40.335250, -3.877680), 30.0), // Aulario 3
        Building(GeoPoint(40.334555, -3.876989), 25.0), // Cafetería
        Building(GeoPoint(40.333580, -3.876020), 40.0), // Rectorado / Gestión
        Building(GeoPoint(40.336850, -3.878520), 35.0), // Departamental 1
        Building(GeoPoint(40.337550, -3.877850), 35.0), // Departamental 2
        Building(GeoPoint(40.334120, -3.878450), 45.0), // Laboratorios
        Building(GeoPoint(40.332500, -3.877200), 35.0)  // Polideportivo / Otros
    )

    val greenZones = listOf(
        GreenZone(GeoPoint(40.336500, -3.877000), 100.0), // Zona verde norte
        GreenZone(GeoPoint(40.334000, -3.877500), 80.0),  // Zona verde sur
        GreenZone(GeoPoint(40.335500, -3.875000), 70.0),  // Zona verde este
        GreenZone(GeoPoint(40.333500, -3.878500), 60.0)   // Zona verde oeste
    )

    fun isUnsafe(point: GeoPoint): Boolean {
        return darkZones.any { zone ->
            distanceMeters(point.latitude, point.longitude, zone.center.latitude, zone.center.longitude) <= zone.radiusMeters
        }
    }

    fun isInsideBuilding(point: GeoPoint): Boolean {
        return buildings.any { b ->
            distanceMeters(point.latitude, point.longitude, b.center.latitude, b.center.longitude) <= b.radiusMeters
        }
    }

    fun isInsideGreenZone(point: GeoPoint): Boolean {
        return greenZones.any { zone ->
            distanceMeters(point.latitude, point.longitude, zone.center.latitude, zone.center.longitude) <= zone.radiusMeters
        }
    }

    data class DarkZone(val center: GeoPoint, val radiusMeters: Double)
    data class Building(val center: GeoPoint, val radiusMeters: Double)
    data class GreenZone(val center: GeoPoint, val radiusMeters: Double)

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val l1 = Math.toRadians(lon1)
        val l2 = Math.toRadians(lon2)
        return acos((sin(p1) * sin(p2)) + (cos(p1) * cos(p2) * cos(l2 - l1))) * r
    }
}
