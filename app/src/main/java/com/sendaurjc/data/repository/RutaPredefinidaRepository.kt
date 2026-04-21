package com.sendaurjc.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sendaurjc.data.local.RutaPredefinida
import org.osmdroid.util.GeoPoint
import java.io.InputStreamReader

class RutaPredefinidaRepository(private val context: Context) {
    private val gson = Gson()

    fun getDestinosConRuta(): Set<String> {
        return try {
            val inputStream = context.assets.open("rutas_predefinidas.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<RutaPredefinida>>() {}.type
            val rutas: List<RutaPredefinida> = gson.fromJson(reader, type)
            rutas.map { it.destino.lowercase() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun getRutasParaDestino(nombreDestino: String): List<Pair<List<GeoPoint>, Double>>? {
        return try {
            val inputStream = context.assets.open("rutas_predefinidas.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<RutaPredefinida>>() {}.type
            val rutas: List<RutaPredefinida> = gson.fromJson(reader, type)
            
            rutas.find { it.destino.equals(nombreDestino, ignoreCase = true) }?.toRouteDataList()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
