package com.sendaurjc.data.local

import org.osmdroid.util.GeoPoint

data class CampusFeature(
    val name: String,
    val type: String, // "building", "green_zone"
    val points: List<GeoPoint>
) {
    val center: GeoPoint by lazy {
        if (points.isEmpty()) GeoPoint(0.0, 0.0)
        else {
            val avgLat = points.map { it.latitude }.average()
            val avgLon = points.map { it.longitude }.average()
            GeoPoint(avgLat, avgLon)
        }
    }
}
