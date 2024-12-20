package com.example.act22.data.repository

import com.example.act22.data.model.Asset
import com.example.act22.data.model.AssetType
import com.example.act22.data.model.SortingCriteria
import com.example.act22.data.model.TechStock
import com.example.act22.data.model.Crypto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AssetRepositoryTestImp : AssetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val stocksCollection = db.collection("stocks")
    private val cryptoCollection = db.collection("crypto")

    override suspend fun getAllAssets(): List<Asset> {
        return try {
            val stocks = stocksCollection.get().await().documents.mapNotNull { doc ->
                TechStock(
                    ID = doc.getString("ticker") ?: return@mapNotNull null,
                    name = doc.getString("ticker") ?: return@mapNotNull null,
                    price = doc.getDouble("price") ?: 0.0,
                    iconUrl = "",
                    sector = "",
                    high = doc.getDouble("high") ?: 0.0,
                    low = doc.getDouble("low") ?: 0.0,
                    open = doc.getDouble("open") ?: 0.0,
                    previous_close = doc.getDouble("previous_close") ?: 0.0,
                    timestamp = doc.getString("timestamp") ?: ""
                )
            }
            
            val cryptos = cryptoCollection.get().await().documents.mapNotNull { doc ->
                Crypto(
                    ID = doc.getString("symbol") ?: return@mapNotNull null,
                    name = doc.getString("symbol") ?: return@mapNotNull null,
                    price = doc.getDouble("price") ?: 0.0,
                    iconUrl = "",
                    blockchain = "",
                    high = doc.getDouble("high") ?: 0.0,
                    low = doc.getDouble("low") ?: 0.0,
                    previous_close = doc.getDouble("previous_close") ?: 0.0,
                    timestamp = doc.getString("timestamp") ?: ""
                )
            }
            
            stocks + cryptos
        } catch (e: Exception) {
            println("Error fetching assets: ${e.message}")
            emptyList()
        }
    }

    override suspend fun searchAssets(search: String): List<Asset> {
        return try {
            val stocks = stocksCollection
                .whereGreaterThanOrEqualTo("ticker", search)
                .whereLessThanOrEqualTo("ticker", search + '\uf8ff')
                .get().await().documents.mapNotNull { doc ->
                    TechStock(
                        ID = doc.getString("ticker") ?: return@mapNotNull null,
                        name = doc.getString("ticker") ?: return@mapNotNull null,
                        price = doc.getDouble("price") ?: 0.0,
                        iconUrl = "",
                        sector = "",
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        open = doc.getDouble("open") ?: 0.0,
                        previous_close = doc.getDouble("previous_close") ?: 0.0,
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                }
            
            val cryptos = cryptoCollection
                .whereGreaterThanOrEqualTo("symbol", search)
                .whereLessThanOrEqualTo("symbol", search + '\uf8ff')
                .get().await().documents.mapNotNull { doc ->
                    Crypto(
                        ID = doc.getString("symbol") ?: return@mapNotNull null,
                        name = doc.getString("symbol") ?: return@mapNotNull null,
                        price = doc.getDouble("price") ?: 0.0,
                        iconUrl = "",
                        blockchain = "",
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        previous_close = doc.getDouble("previous_close") ?: 0.0,
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                }
            
            stocks + cryptos
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun filterAssetsByType(type: AssetType): List<Asset> {
        return try {
            when (type) {
                AssetType.STOCK -> stocksCollection.get().await().documents.mapNotNull { doc ->
                    TechStock(
                        ID = doc.getString("ticker") ?: return@mapNotNull null,
                        name = doc.getString("ticker") ?: return@mapNotNull null,
                        price = doc.getDouble("price") ?: 0.0,
                        iconUrl = "",
                        sector = "",
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        open = doc.getDouble("open") ?: 0.0,
                        previous_close = doc.getDouble("previous_close") ?: 0.0,
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                }
                AssetType.CRYPTO -> cryptoCollection.get().await().documents.mapNotNull { doc ->
                    Crypto(
                        ID = doc.getString("symbol") ?: return@mapNotNull null,
                        name = doc.getString("symbol") ?: return@mapNotNull null,
                        price = doc.getDouble("price") ?: 0.0,
                        iconUrl = "",
                        blockchain = "",
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        previous_close = doc.getDouble("previous_close") ?: 0.0,
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                }
                AssetType.ALL -> getAllAssets()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun sortAssets(criteria: SortingCriteria): List<Asset> {
        val assets = getAllAssets()
        return when (criteria) {
            SortingCriteria.ASC -> assets.sortedBy { it.price }
            SortingCriteria.DESC -> assets.sortedByDescending { it.price }
            SortingCriteria.ALPHABET -> assets.sortedBy { it.name }
        }
    }

    override suspend fun findAsset(id: String): Asset {
        try {
            // Try stocks first
            val stockDoc = stocksCollection.whereEqualTo("ticker", id).get().await().documents.firstOrNull()
            if (stockDoc != null) {
                return TechStock(
                    ID = stockDoc.getString("ticker") ?: throw Exception("Asset not found"),
                    name = stockDoc.getString("ticker") ?: throw Exception("Asset not found"),
                    price = stockDoc.getDouble("price") ?: 0.0,
                    iconUrl = "",
                    sector = "",
                    high = stockDoc.getDouble("high") ?: 0.0,
                    low = stockDoc.getDouble("low") ?: 0.0,
                    open = stockDoc.getDouble("open") ?: 0.0,
                    previous_close = stockDoc.getDouble("previous_close") ?: 0.0,
                    timestamp = stockDoc.getString("timestamp") ?: ""
                )
            }

            // Try cryptos if not found in stocks
            val cryptoDoc = cryptoCollection.whereEqualTo("symbol", id).get().await().documents.firstOrNull()
            if (cryptoDoc != null) {
                return Crypto(
                    ID = cryptoDoc.getString("symbol") ?: throw Exception("Asset not found"),
                    name = cryptoDoc.getString("symbol") ?: throw Exception("Asset not found"),
                    price = cryptoDoc.getDouble("price") ?: 0.0,
                    iconUrl = "",
                    blockchain = "",
                    high = cryptoDoc.getDouble("high") ?: 0.0,
                    low = cryptoDoc.getDouble("low") ?: 0.0,
                    previous_close = cryptoDoc.getDouble("previous_close") ?: 0.0,
                    timestamp = cryptoDoc.getString("timestamp") ?: ""
                )
            }

            throw Exception("Asset not found")
        } catch (e: Exception) {
            throw Exception("Failed to fetch asset: ${e.message}")
        }
    }

    override suspend fun getYearlyHistoryPricePoints(id: String): List<Double> {
        return try {
            val priceHistory = db.collection("priceHistory")
                .document(id)
                .collection("yearly")
                .get().await()
            
            priceHistory.documents.mapNotNull { it.getDouble("price") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getDailyHistoryPricePoints(id: String): List<Double> {
        return try {
            val priceHistory = db.collection("priceHistory")
                .document(id)
                .collection("daily")
                .get().await()
            
            priceHistory.documents.mapNotNull { it.getDouble("price") }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
