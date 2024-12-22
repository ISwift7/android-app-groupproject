package com.example.act22.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://humane-jaybird-genuinely.ngrok-free.app/android/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tradingApi: TradingApi by lazy {
        retrofit.create(TradingApi::class.java)
    }

    val graphApi: GraphApi by lazy {
        retrofit.create(GraphApi::class.java)
    }
} 