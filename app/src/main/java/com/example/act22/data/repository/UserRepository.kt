package com.example.act22.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isPremiumCache: Boolean? = null
    private var hasPayedForPremiumCache: Boolean? = null

    private fun getCurrentUserEmail(): String {
        return auth.currentUser?.email ?: throw IllegalStateException("User not logged in")
    }

    // Check if the user is a premium user with caching
    suspend fun isPremiumUser(): Boolean {
        // Return cached value if available
        isPremiumCache?.let {
            return it
        }

        return try {
            val userQuery = db.collection("androidUsers")
                .whereEqualTo("email", getCurrentUserEmail())
                .limit(1)
                .get()
                .await()

            val userDoc = userQuery.documents.firstOrNull()

            if (userDoc != null) {
                val isPremium = userDoc.getBoolean("isPremium")
                if (isPremium == null) {
                    userDoc.reference.update("isPremium", false)
                    isPremiumCache = false
                    false
                } else {
                    isPremiumCache = isPremium
                    isPremium
                }
            } else {
                isPremiumCache = false
                false
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error checking premium status: ${e.message}")
            isPremiumCache = false // Default to false on error
            false
        }
    }

    // Check if the user has previously paid for premium with caching
    suspend fun hasUserPayedBefore(): Boolean {
        // Return cached value if available
        hasPayedForPremiumCache?.let {
            return it
        }

        return try {
            val userQuery = db.collection("androidUsers")
                .whereEqualTo("email", getCurrentUserEmail())
                .limit(1)
                .get()
                .await()

            val userDoc = userQuery.documents.firstOrNull()

            if (userDoc != null) {
                val hasPayedForPremium = userDoc.getBoolean("hasPayedForPremium")
                if (hasPayedForPremium == null) {
                    userDoc.reference.update("hasPayedForPremium", false)
                    hasPayedForPremiumCache = false
                    false
                } else {
                    hasPayedForPremiumCache = hasPayedForPremium
                    hasPayedForPremium
                }
            } else {
                hasPayedForPremiumCache = false
                false
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error checking payment status: ${e.message}")
            hasPayedForPremiumCache = false // Default to false on error
            false
        }
    }

    // Update the user's premium plan and clear the cache
    suspend fun updateUserPlan(isNowPremium: Boolean) {
        try {
            val userQuery = db.collection("androidUsers")
                .whereEqualTo("email", getCurrentUserEmail())
                .limit(1)
                .get()
                .await()

            val userDoc = userQuery.documents.firstOrNull()

            userDoc?.reference?.update("isPremium", isNowPremium)

            // Update the cache
            isPremiumCache = isNowPremium
        } catch (e: Exception) {
            Log.e("Firestore", "Error changing premium status: ${e.message}")
        }
    }

    suspend fun recordPayment(){
        try {
            val userQuery = db.collection("androidUsers")
                .whereEqualTo("email", getCurrentUserEmail())
                .limit(1)
                .get()
                .await()

            val userDoc = userQuery.documents.firstOrNull()

            userDoc?.reference?.update("hasPayedForPremium", true)

            // Update the cache
            isPremiumCache = true
        } catch (e: Exception) {
            Log.e("Firestore", "Error recoding premium status: ${e.message}")
        }
    }

    // Clear cached data manually
    fun clearCache() {
        isPremiumCache = null
        hasPayedForPremiumCache = null
    }
}
