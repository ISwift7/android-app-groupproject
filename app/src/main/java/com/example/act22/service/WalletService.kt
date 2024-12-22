package com.example.act22.service

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletService {
    private val client = OkHttpClient()
    private val baseUrl = "https://backend-hh3k.onrender.com/android"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createPaymentIntent(amount: Double, idToken: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("amount", amount)
            }
            
            println("Creating payment intent with amount: $amount")
            println("Request body: ${json.toString()}")

            val request = Request.Builder()
                .url("$baseUrl/wallet/create-payment-intent")
                .addHeader("Authorization", "Bearer $idToken")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            println("Payment intent response code: ${response.code}")
            println("Payment intent response body: $responseBody")
            
            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                Result.success(jsonResponse.getString("clientSecret"))
            } else {
                val errorMessage = if (responseBody != null) {
                    try {
                        JSONObject(responseBody).optString("error", "Unknown error")
                    } catch (e: Exception) {
                        responseBody
                    }
                } else {
                    "Failed to create payment intent: ${response.code}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("Payment intent error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun confirmPayment(clientSecret: String, amount: Double, idToken: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("client_secret", clientSecret)
                put("amount", amount)
            }

            val request = Request.Builder()
                .url("$baseUrl/wallet/confirm-payment")
                .addHeader("Authorization", "Bearer $idToken")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = JSONObject(response.body?.string() ?: "")
                Result.success(responseBody.getDouble("new_balance"))
            } else {
                Result.failure(Exception("Failed to confirm payment: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBalance(idToken: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/wallet/get-balance")
                .addHeader("Authorization", "Bearer $idToken")
                .get()
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = JSONObject(response.body?.string() ?: "")
                Result.success(responseBody.getDouble("wallet_balance"))
            } else {
                Result.failure(Exception("Failed to get balance: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun withdrawFunds(amount: Double, idToken: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("amount", amount)
            }

            val request = Request.Builder()
                .url("$baseUrl/wallet/withdraw")
                .addHeader("Authorization", "Bearer $idToken")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = JSONObject(response.body?.string() ?: "")
                Result.success(responseBody.getDouble("new_balance"))
            } else {
                Result.failure(Exception("Failed to withdraw funds: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactions(idToken: String): Result<List<Map<String, Any>>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/wallet/transactions")
                .addHeader("Authorization", "Bearer $idToken")
                .get()
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = JSONObject(response.body?.string() ?: "")
                val transactions = responseBody.getJSONArray("transactions")
                val transactionsList = mutableListOf<Map<String, Any>>()
                
                for (i in 0 until transactions.length()) {
                    val transaction = transactions.getJSONObject(i)
                    transactionsList.add(transaction.toMap())
                }
                
                Result.success(transactionsList)
            } else {
                Result.failure(Exception("Failed to get transactions: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        this.keys().forEach { key ->
            when (val value = this.get(key)) {
                is JSONObject -> map[key] = value.toMap()
                is Boolean, is Int, is Long, is Double, is String -> map[key] = value
                else -> map[key] = value.toString()
            }
        }
        return map
    }
} 