package com.example.act22.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://backend-hh3k.onrender.com/android/"

    private val tradingHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val graphHttpClient = OkHttpClient.Builder()
        .connectTimeout(40, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val tradingRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(tradingHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val graphRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(graphHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tradingApi: TradingApi by lazy {
        tradingRetrofit.create(TradingApi::class.java)
    }

    val graphApi: GraphApi by lazy {
        graphRetrofit.create(GraphApi::class.java)
    }
} 