package com.sendaurjc.data.local

import org.osmdroid.util.GeoPoint

data class Sitio(
    val nombre: String,
    val latitud: Double,
    val longitud: Double
) {
    val coordenadas: GeoPoint get() = GeoPoint(latitud, longitud)
}
