package com.sendaurjc.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendaurjc.data.local.RutaPredefinida
import org.osmdroid.util.GeoPoint
import java.io.InputStreamReader

class RutaPredefinidaRepository(private val context: Context) {
    private val gson = Gson()

    fun getRutaParaDestino(nombreDestino: String): List<GeoPoint>? {
        return try {
            val inputStream = context.assets.open("rutas_predefinidas.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<RutaPredefinida>>() {}.type
            val rutas: List<RutaPredefinida> = gson.fromJson(reader, type)
            
            rutas.find { it.destino.equals(nombreDestino, ignoreCase = true) }?.toGeoPoints()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
