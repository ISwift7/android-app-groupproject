package com.example.act22.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.act22.data.model.Asset
import com.example.act22.data.model.TechStock
import com.example.act22.data.model.Crypto

class FirestoreAssetRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getAssetPrice(assetId: String, isStock: Boolean): Double {
        val collection = if (isStock) "stocks" else "crypto"
        val field = if (isStock) "ticker" else "symbol"
        
        return try {
            val snapshot = db.collection(collection)
                .whereEqualTo(field, assetId)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].getDouble("price") ?: 0.0
            } else {
                0.0
            }
        } catch (e: Exception) {
            println("Error fetching asset price: ${e.message}")
            0.0
        }
    }

    suspend fun getStockData(ticker: String): TechStock? {
        return try {
            val snapshot = db.collection("stocks")
                .whereEqualTo("ticker", ticker)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                TechStock(
                    ID = doc.getString("ticker") ?: return null,
                    name = doc.getString("ticker") ?: return null,
                    price = doc.getDouble("price") ?: 0.0,
                    iconUrl = "",
                    sector = "",
                    high = doc.getDouble("high") ?: 0.0,
                    low = doc.getDouble("low") ?: 0.0,
                    open = doc.getDouble("open") ?: 0.0,
                    previous_close = doc.getDouble("previous_close") ?: 0.0,
                    timestamp = doc.getString("timestamp") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching stock data: ${e.message}")
            null
        }
    }

    suspend fun getCryptoData(symbol: String): Crypto? {
        return try {
            val snapshot = db.collection("crypto")
                .whereEqualTo("symbol", symbol)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                Crypto(
                    ID = doc.getString("symbol") ?: return null,
                    name = doc.getString("symbol") ?: return null,
                    price = doc.getDouble("price") ?: 0.0,
                    iconUrl = "",
                    blockchain = "",
                    high = doc.getDouble("high") ?: 0.0,
                    low = doc.getDouble("low") ?: 0.0,
                    previous_close = doc.getDouble("previous_close") ?: 0.0,
                    timestamp = doc.getString("timestamp") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error fetching crypto data: ${e.message}")
            null
        }
    }
} 