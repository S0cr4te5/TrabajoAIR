package com.sendaurjc.data.local

import org.osmdroid.util.GeoPoint

data class RutaPredefinida(
    val destino: String,
    val puntos: List<PuntoRuta>
) {
    data class PuntoRuta(val lat: Double, val lon: Double)
    
    fun toGeoPoints(): List<GeoPoint> = puntos.map { GeoPoint(it.lat, it.lon) }
}
