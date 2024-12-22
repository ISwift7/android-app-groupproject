package com.example.act22.service

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackendService {
    private val client = OkHttpClient()
    private val baseUrl = "https://backend-hh3k.onrender.com/android"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createUser(email: String, idToken: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
            }

            val request = Request.Builder()
                .url("$baseUrl/user/create")
                .addHeader("Authorization", "Bearer $idToken")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create user: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
