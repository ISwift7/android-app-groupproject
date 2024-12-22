package com.example.act22.data.repository

import com.example.act22.data.model.Asset
import com.example.act22.data.model.PriceAlert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PriceAlertRepositoryFirebaseImpl : PriceAlertRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: throw IllegalStateException("User not logged in")
    }

    override suspend fun getPriceAlertsForAsset(asset: Asset): List<PriceAlert> {
        try {
            // Get user document reference
            val userQuery = db.collection("androidUsers")
                .whereEqualTo("email", getCurrentUserEmail())
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                return emptyList()
            }

            val userRef = userQuery.documents[0].reference
            
            // Get active alerts for this asset
            val alertsQuery = userRef.collection("price_alerts")
                .whereEqualTo("symbol", asset.ID)
                .whereEqualTo("status", "active")
                .get()
                .await()

            return alertsQuery.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                PriceAlert(
                    id = doc.id,
                    symbol = data["symbol"] as? String ?: return@mapNotNull null,
                    is_crypto = data["is_crypto"] as? Boolean ?: return@mapNotNull null,
                    target_price = (data["target_price"] as? Number)?.toDouble() ?: return@mapNotNull null,
                    alert_type = data["alert_type"] as? String ?: return@mapNotNull null,
                    current_price_at_creation = (data["current_price_at_creation"] as? Number)?.toDouble() ?: return@mapNotNull null,
                    status = data["status"] as? String ?: "active",
                    created_at = data["created_at"]?.toString() ?: "",
                    triggered_at = data["triggered_at"]?.toString(),
                    price_at_trigger = (data["price_at_trigger"] as? Number)?.toDouble()
                )
            }
        } catch (e: Exception) {
            println("Error getting price alerts: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun addPriceAlert(priceAlert: PriceAlert) {
        try {
            // Get user document reference
            val userQuery = db.collection("androidUsers")
                .whereEqualTo("email", getCurrentUserEmail())
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                throw IllegalStateException("User not found")
            }

            val userRef = userQuery.documents[0].reference
            
            // Add alert to user's price_alerts collection
            userRef.collection("price_alerts").add(
                hashMapOf(
                    "symbol" to priceAlert.symbol,
                    "is_crypto" to priceAlert.is_crypto,
                    "target_price" to priceAlert.target_price,
                    "alert_type" to priceAlert.alert_type,
                    "current_price_at_creation" to priceAlert.current_price_at_creation,
                    "status" to priceAlert.status,
                    "created_at" to priceAlert.created_at,
                    "triggered_at" to priceAlert.triggered_at,
                    "price_at_trigger" to priceAlert.price_at_trigger
                )
            ).await()
        } catch (e: Exception) {
            println("Error adding price alert: ${e.message}")
            throw e
        }
    }

    override suspend fun deletePriceAlert(priceAlert: PriceAlert) {
        try {
            // Get user document reference
            val userQuery = db.collection("androidUsers")
                .whereEqualTo("email", getCurrentUserEmail())
                .limit(1)
                .get()
                .await()

            if (userQuery.isEmpty) {
                throw IllegalStateException("User not found")
            }

            val userRef = userQuery.documents[0].reference
            
            // Delete the alert document
            userRef.collection("price_alerts").document(priceAlert.id).delete().await()
        } catch (e: Exception) {
            println("Error deleting price alert: ${e.message}")
            throw e
        }
    }

    override suspend fun checkPriceAlerts(asset: Asset): List<PriceAlert> {
        // This is handled by the backend
        return emptyList()
    }
} 