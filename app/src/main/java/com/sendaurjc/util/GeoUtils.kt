package com.sendaurjc.util

import org.osmdroid.util.GeoPoint
import kotlin.math.*

object GeoUtils {
    fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(h))
    }

    fun interpolate(a: GeoPoint, b: GeoPoint, fraction: Double): GeoPoint {
        val lat = a.latitude + (b.latitude - a.latitude) * fraction
        val lon = a.longitude + (b.longitude - a.longitude) * fraction
        return GeoPoint(lat, lon)
    }
}
