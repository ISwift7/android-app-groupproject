package com.example.act22.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

data class GraphDataPoint(
    val timestamp: String,
    val price: Double,
    val high: Double,
    val low: Double,
    val open: Double,
    val previous_close: Double
)

data class GraphData(
    val symbol: String,
    val is_crypto: Boolean,
    val points: List<GraphDataPoint>
)

data class GraphResponse(
    val status: String,
    val data: GraphData
)

interface GraphApi {
    @GET("asset/{symbol}/graph")
    suspend fun getAssetGraph(
        @Header("Authorization") token: String,
        @Path("symbol") symbol: String,
        @Query("is_crypto") isCrypto: Boolean
    ): Response<GraphResponse>
} 