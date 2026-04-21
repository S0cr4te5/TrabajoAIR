package com.sendaurjc.data.network

import com.sendaurjc.data.mock.MockLumenSmartDataSource.Building
import com.sendaurjc.data.mock.MockLumenSmartDataSource.GreenZone
import okhttp3.OkHttpClient
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import kotlin.math.cos
import kotlin.math.sin

class OverpassService {
    private val api: OverpassApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/api/")
            .client(client)
            .build()
            .create(OverpassApi::class.java)
    }

    suspend fun fetchBuildings(
        centerLat: Double,
        centerLon: Double,
        radiusKm: Double = 0.5
    ): List<Building> {
        return try {
            val (minLat, maxLat, minLon, maxLon) = calculateBoundingBox(centerLat, centerLon, radiusKm)

            // Consulta Overpass para edificios
            val query = """
                [bbox:$minLat,$minLon,$maxLat,$maxLon];
                (way["building"];relation["building"];);
                out center;
            """.trimIndent()

            val response = api.query(query)
            parseBuildings(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun fetchGreenZones(
        centerLat: Double,
        centerLon: Double,
        radiusKm: Double = 0.5
    ): List<GreenZone> {
        return try {
            val (minLat, maxLat, minLon, maxLon) = calculateBoundingBox(centerLat, centerLon, radiusKm)

            // Consulta Overpass para zonas verdes
            val query = """
                [bbox:$minLat,$minLon,$maxLat,$maxLon];
                (way["leisure"~"park|garden|playground"];
                 way["landuse"~"forest|grass|meadow|greenfield"];
                 way["natural"~"wood|grassland"];
                 relation["leisure"~"park|garden|playground"];
                 relation["landuse"~"forest|grass|meadow|greenfield"];
                 relation["natural"~"wood|grassland"];
                );
                out center;
            """.trimIndent()

            val response = api.query(query)
            parseGreenZones(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseBuildings(osmResponse: String): List<Building> {
        val buildings = mutableListOf<Building>()

        try {
            // Parse simple XML response from Overpass
            val centerPattern = """<center lat="([\d\.\-]+)" lon="([\d\.\-]+)"/>""".toRegex()
            val wayPattern = """<way[^>]*>.*?<center lat="([\d\.\-]+)" lon="([\d\.\-]+)"/>[^<]*</way>""".toRegex(RegexOption.DOT_MATCHES_ALL)

            wayPattern.findAll(osmResponse).forEach { match ->
                try {
                    val lat = match.groupValues[1].toDouble()
                    val lon = match.groupValues[2].toDouble()
                    buildings.add(Building(
                        center = GeoPoint(lat, lon),
                        radiusMeters = 30.0 // Valor por defecto
                    ))
                } catch (e: Exception) {
                    // Skip parsing error
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return buildings
    }

    private fun parseGreenZones(osmResponse: String): List<GreenZone> {
        val zones = mutableListOf<GreenZone>()

        try {
            val wayPattern = """<way[^>]*>.*?<center lat="([\d\.\-]+)" lon="([\d\.\-]+)"/>[^<]*</way>""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val relationPattern = """<relation[^>]*>.*?<center lat="([\d\.\-]+)" lon="([\d\.\-]+)"/>[^<]*</relation>""".toRegex(RegexOption.DOT_MATCHES_ALL)

            wayPattern.findAll(osmResponse).forEach { match ->
                try {
                    val lat = match.groupValues[1].toDouble()
                    val lon = match.groupValues[2].toDouble()
                    zones.add(GreenZone(
                        center = GeoPoint(lat, lon),
                        radiusMeters = 50.0 // Valor por defecto
                    ))
                } catch (e: Exception) {
                    // Skip
                }
            }

            relationPattern.findAll(osmResponse).forEach { match ->
                try {
                    val lat = match.groupValues[1].toDouble()
                    val lon = match.groupValues[2].toDouble()
                    zones.add(GreenZone(
                        center = GeoPoint(lat, lon),
                        radiusMeters = 80.0 // Relaciones suelen ser más grandes
                    ))
                } catch (e: Exception) {
                    // Skip
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return zones
    }

    private fun calculateBoundingBox(
        centerLat: Double,
        centerLon: Double,
        radiusKm: Double
    ): Tuple4 {
        val latDelta = radiusKm / 111.0 // 1 grado de latitud ~ 111 km
        val lonDelta = radiusKm / (111.0 * cos(Math.toRadians(centerLat)))

        return Tuple4(
            minLat = centerLat - latDelta,
            maxLat = centerLat + latDelta,
            minLon = centerLon - lonDelta,
            maxLon = centerLon + lonDelta
        )
    }

    private data class Tuple4(
        val minLat: Double,
        val maxLat: Double,
        val minLon: Double,
        val maxLon: Double
    )
}

