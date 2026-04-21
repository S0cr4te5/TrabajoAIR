package com.sendaurjc.domain

import com.sendaurjc.data.network.OsrmApi
import com.sendaurjc.data.mock.MockLumenSmartDataSource
import com.sendaurjc.util.GeoUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RouteRepository {
    private val routingService = RoutingService()
    private val api: OsrmApi by lazy {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder().addInterceptor(logger).build()
        Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsrmApi::class.java)
    }

    suspend fun requestWalkingRoute(origin: GeoPoint, destination: GeoPoint): List<GeoPoint> {
        val coord = "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}"
        val response = try { api.route(coord) } catch (e: Exception) { null }
        val points = response?.routes?.firstOrNull()?.geometry?.coordinates.orEmpty()
        
        val osrmRoute = points.mapNotNull {
            if (it.size >= 2) GeoPoint(it[1], it[0]) else null
        }

        // Una ruta es segura si existe y ningún tramo pasa por zona peligrosa
        val isSafe = osrmRoute.isNotEmpty() && isPathSafe(osrmRoute)

        val finalRoute = if (isSafe) {
            osrmRoute
        } else {
            routingService.findSafeRoute(origin, destination)
        }

        // Forzamos que el primer y último punto sean EXACTAMENTE los seleccionados
        return if (finalRoute.size >= 2) {
            listOf(origin) + finalRoute.drop(1).dropLast(1) + destination
        } else {
            listOf(origin, destination)
        }
    }

    private fun isPathSafe(route: List<GeoPoint>): Boolean {
        for (i in 0 until route.lastIndex) {
            val start = route[i]
            val end = route[i + 1]
            val dist = GeoUtils.distanceMeters(start, end)
            val steps = (dist / 10).toInt().coerceAtLeast(1)
            for (j in 0..steps) {
                val point = GeoUtils.interpolate(start, end, j.toDouble() / steps)
                // Evita zonas oscuras, edificios y zonas verdes
                if (MockLumenSmartDataSource.isUnsafe(point)) return false
                if (MockLumenSmartDataSource.isInsideBuilding(point)) return false
                if (MockLumenSmartDataSource.isInsideGreenZone(point)) return false
            }
        }
        return true
    }
}
