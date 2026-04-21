package com.sendaurjc.data.local

import org.osmdroid.util.GeoPoint

data class RutaPredefinida(
    val destino: String,
    val rutas: List<DetalleRuta>
) {
    data class DetalleRuta(
        val puntos: List<PuntoRuta>,
        val modificador_indice: Double
    )
    
    data class PuntoRuta(val lat: Double, val lon: Double)
    
    fun toRouteDataList(): List<Pair<List<GeoPoint>, Double>> = rutas.map { ruta -> 
        ruta.puntos.map { GeoPoint(it.lat, it.lon) } to ruta.modificador_indice
    }
}
