package com.example.act22.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class GraphDataPoint(
    val timestamp: String,
    val price: Double,
    val high: Double,
    val low: Double,
    val open: Double,
    val previous_close: Double
)

interface GraphApi {
    @GET("user/get_historical_data")
    suspend fun getAssetGraph(
        @Header("Authorization") token: String,
        @Query("ticker") symbol: String,
        @Query("type") type: String
    ): Response<List<GraphDataPoint>>
} 