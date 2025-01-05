package com.example.act22.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://backend-hh3k.onrender.com/android/"
    private const val AI_URL = "https://fe11-217-183-59-76.ngrok-free.app/"

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

    private val aiHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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

    private val aiRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AI_URL)
            .client(aiHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tradingApi: TradingApi by lazy {
        tradingRetrofit.create(TradingApi::class.java)
    }

    val graphApi: GraphApi by lazy {
        graphRetrofit.create(GraphApi::class.java)
    }

    val aiApi: AIApi by lazy {
        aiRetrofit.create(AIApi::class.java)
    }
} 