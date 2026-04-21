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

    // Datos por defecto (hardcodeados como fallback)
    private val defaultBuildings = listOf(
        Building(GeoPoint(40.337083, -3.875583), 35.0),
        Building(GeoPoint(40.335802, -3.876332), 30.0),
        Building(GeoPoint(40.336420, -3.877150), 30.0),
        Building(GeoPoint(40.335250, -3.877680), 30.0),
        Building(GeoPoint(40.334555, -3.876989), 25.0),
        Building(GeoPoint(40.333580, -3.876020), 40.0),
        Building(GeoPoint(40.336850, -3.878520), 35.0),
        Building(GeoPoint(40.337550, -3.877850), 35.0),
        Building(GeoPoint(40.334120, -3.878450), 45.0),
        Building(GeoPoint(40.332500, -3.877200), 35.0)
    )

    private val defaultGreenZones = listOf(
        GreenZone(GeoPoint(40.336500, -3.877000), 100.0),
        GreenZone(GeoPoint(40.334000, -3.877500), 80.0),
        GreenZone(GeoPoint(40.335500, -3.875000), 70.0),
        GreenZone(GeoPoint(40.333500, -3.878500), 60.0)
    )

    // Variables para almacenar datos dinámicos
    private var _buildings = defaultBuildings.toMutableList()
    private var _greenZones = defaultGreenZones.toMutableList()

    // Propiedades públicas
    val buildings: List<Building>
        get() = _buildings

    val greenZones: List<GreenZone>
        get() = _greenZones

    /**
     * Actualiza los edificios con datos obtenidos de una fuente externa (ej: Overpass API)
     * @param newBuildings Lista de nuevos edificios
     */
    fun updateBuildings(newBuildings: List<Building>) {
        _buildings = if (newBuildings.isNotEmpty()) {
            newBuildings.toMutableList()
        } else {
            defaultBuildings.toMutableList()
        }
    }

    /**
     * Actualiza las zonas verdes con datos obtenidos de una fuente externa
     * @param newZones Lista de nuevas zonas verdes
     */
    fun updateGreenZones(newZones: List<GreenZone>) {
        _greenZones = if (newZones.isNotEmpty()) {
            newZones.toMutableList()
        } else {
            defaultGreenZones.toMutableList()
        }
    }

    private var _campusFeatures = listOf<com.sendaurjc.data.local.CampusFeature>()

    fun setFeatures(features: List<com.sendaurjc.data.local.CampusFeature>) {
        _campusFeatures = features
        
        // Actualizar edificios y zonas verdes para mantener compatibilidad si es necesario
        _buildings = features.filter { it.type == "building" }.map { 
            Building(it.center, 30.0) 
        }.toMutableList()
        
        _greenZones = features.filter { it.type == "green_zone" }.map { 
            GreenZone(it.center, 50.0) 
        }.toMutableList()
    }

    fun isUnsafe(point: GeoPoint): Boolean {
        for (zone in darkZones) {
            if (distanceMetersSquared(point.latitude, point.longitude, zone.center.latitude, zone.center.longitude) <= zone.radiusMeters * zone.radiusMeters) {
                return true
            }
        }
        return false
    }

    fun isInsideBuilding(point: GeoPoint): Boolean {
        for (b in _buildings) {
            if (distanceMetersSquared(point.latitude, point.longitude, b.center.latitude, b.center.longitude) <= b.radiusMeters * b.radiusMeters) {
                return true
            }
        }
        return false
    }

    fun isInsideGreenZone(point: GeoPoint): Boolean {
        for (zone in _greenZones) {
            if (distanceMetersSquared(point.latitude, point.longitude, zone.center.latitude, zone.center.longitude) <= zone.radiusMeters * zone.radiusMeters) {
                return true
            }
        }
        return false
    }

    data class DarkZone(val center: GeoPoint, val radiusMeters: Double)
    data class Building(val center: GeoPoint, val radiusMeters: Double)
    data class GreenZone(val center: GeoPoint, val radiusMeters: Double)

    // Cálculo ultra rápido sin raíz cuadrada
    private fun distanceMetersSquared(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val p1 = Math.toRadians(lat1)
        val p2 = Math.toRadians(lat2)
        val l1 = Math.toRadians(lon1)
        val l2 = Math.toRadians(lon2)
        val angle = acos((sin(p1) * sin(p2)) + (cos(p1) * cos(p2) * cos(l2 - l1)))
        val dist = angle * r
        return dist * dist
    }
}
