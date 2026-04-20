package com.sendaurjc.domain

import com.sendaurjc.data.network.OsrmApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RouteRepository {
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
        val response = api.route(coord)
        val points = response.routes.firstOrNull()?.geometry?.coordinates.orEmpty()
        return points.mapNotNull {
            if (it.size >= 2) GeoPoint(it[1], it[0]) else null
        }
    }
}
