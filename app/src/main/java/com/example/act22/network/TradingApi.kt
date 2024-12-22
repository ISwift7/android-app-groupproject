package com.example.act22.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class PurchaseRequest(
    val symbol: String,
    val is_crypto: Boolean,
    val quantity: Double,
    val price_per_unit: Double,
    val total_cost: Double
)

data class SellRequest(
    val symbol: String,
    val is_crypto: Boolean,
    val quantity: Double,
    val price_per_unit: Double,
    val total_value: Double
)

data class TradingResponse(
    val message: String,
    val transaction: Transaction
)

data class Transaction(
    val symbol: String,
    val quantity: Double,
    val price: Double,
    val type: String,
    val total_cost: Double? = null,
    val total_value: Double? = null,
    val new_balance: Double
)

interface TradingApi {
    @POST("wallet/purchase-asset")
    suspend fun purchaseAsset(
        @Header("Authorization") token: String,
        @Body request: PurchaseRequest
    ): Response<TradingResponse>

    @POST("wallet/sell-asset")
    suspend fun sellAsset(
        @Header("Authorization") token: String,
        @Body request: SellRequest
    ): Response<TradingResponse>
}
