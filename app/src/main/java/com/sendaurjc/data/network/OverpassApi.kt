package com.sendaurjc.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApi {
    @GET("interpreter")
    suspend fun query(@Query("data") query: String): String
}

