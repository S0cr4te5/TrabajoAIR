package com.sendaurjc.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sendaurjc.data.local.CampusFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

class CampusFeatureRepository(private val context: Context) {
    private val gson = Gson()

    suspend fun loadCampusFeatures(): List<CampusFeature> = withContext(Dispatchers.IO) {
        try {
            val json = context.assets.open("data/campus_features.json").bufferedReader().use { it.readText() }
            val root = gson.fromJson(json, JsonObject::class.java)
            val featuresArray = root.getAsJsonArray("features")
            
            featuresArray.map { element ->
                val featureObj = element.asJsonObject
                val properties = featureObj.getAsJsonObject("properties")
                val geometry = featureObj.getAsJsonObject("geometry")
                val coordinates = geometry.getAsJsonArray("coordinates").get(0).asJsonArray
                
                val points = mutableListOf<GeoPoint>()
                coordinates.forEach { coordElement ->
                    val coord = coordElement.asJsonArray
                    points.add(GeoPoint(coord.get(1).asDouble, coord.get(0).asDouble))
                }
                
                CampusFeature(
                    name = properties.get("name").asString,
                    type = properties.get("type").asString,
                    points = points
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
