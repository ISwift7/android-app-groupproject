package com.example.act22.data.repository

import com.example.act22.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import kotlin.Exception

class AssetRepositoryFirebaseImpl : AssetRepository {
    private val db = FirebaseFirestore.getInstance()
    private val stocksCollection = db.collection("stocksCollection")
    private val cryptoCollection = db.collection("cryptoCollection")

    private fun getStockIconUrl(ticker: String): String {
        return when (ticker) {
            "AAPL" -> "https://cdn.prod.website-files.com/62b0e6308cc691625470b227/62dec0259f18b71442a15966_Apple-Logo.png"
            "MSFT" -> "https://cdn.prod.website-files.com/5ee732bebd9839b494ff27cd/5eef3a3260847d0d2783a76d_Microsoft-Logo-PNG-Transparent-Image.png"
            "GOOGL" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/Google_%22G%22_logo.svg/768px-Google_%22G%22_logo.svg.png"
            "AMZN" -> "https://static-00.iconduck.com/assets.00/amazon-icon-512x512-qj1xkn8x.png"
            "TSLA" -> "https://upload.wikimedia.org/wikipedia/commons/e/e8/Tesla_logo.png"
            "NVDA" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Nvidia_logo.svg/1280px-Nvidia_logo.svg.png"
            "META" -> "https://upload.wikimedia.org/wikipedia/commons/a/ab/Meta-Logo.png"
            "INTC" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7d/Intel_logo_%282006-2020%29.svg/1005px-Intel_logo_%282006-2020%29.svg.png"
            "AMD" -> "https://banner2.cleanpng.com/20180403/dkq/avhnmjq47.webp"
            "IBM" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/51/IBM_logo.svg/1000px-IBM_logo.svg.png"
            "CSCO" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Cisco_logo_blue_2016.svg/1200px-Cisco_logo_blue_2016.svg.png"
            "ORCL" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/50/Oracle_logo.svg/2560px-Oracle_logo.svg.png"
            "ADBE" -> "https://i.pinimg.com/736x/56/3a/a2/563aa2189ef92dc242a7db5b91078804.jpg"
            "BA" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Boeing_full_logo.svg/2560px-Boeing_full_logo.svg.png"
            "DIS" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Disney%2B_logo.svg/1200px-Disney%2B_logo.svg.png"
            "JNJ" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/Johnson_and_Johnson_Logo.svg/1280px-Johnson_and_Johnson_Logo.svg.png"
            "JPM" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/JPMorgan_Chase.svg/1280px-JPMorgan_Chase.svg.png"
            "MA" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Mastercard-logo.svg/2560px-Mastercard-logo.svg.png"
            "MRK" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Merck_%26_Co.svg/1280px-Merck_%26_Co.svg.png"
            "NFLX" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Netflix_2015_logo.svg/2560px-Netflix_2015_logo.svg.png"
            "PEP" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/2560px-Pepsi_2023.svg.png"
            "PFE" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8b/Pfizer_logo.svg/1280px-Pfizer_logo.svg.png"
            "PYPL" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/PayPal.svg/2560px-PayPal.svg.png"
            "V" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5e/Visa_Inc._logo.svg/2560px-Visa_Inc._logo.svg.png"
            else -> ""
        }
    }

    private fun getCryptoIconUrl(symbol: String): String {
        return when (symbol) {
            "BTC" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Bitcoin.svg/1200px-Bitcoin.svg.png"
            "ETH" -> "https://cryptologos.cc/logos/ethereum-eth-logo.png"
            "ADA" -> "https://cryptologos.cc/logos/cardano-ada-logo.png"
            "BNB" -> "https://cryptologos.cc/logos/binance-coin-bnb-logo.png"
            "SOL" -> "https://cryptologos.cc/logos/solana-sol-logo.png"
            "DOT" -> "https://cryptologos.cc/logos/polkadot-new-dot-logo.png"
            "AVAX" -> "https://cryptologos.cc/logos/avalanche-avax-logo.png"
            "LINK" -> "https://cryptologos.cc/logos/chainlink-link-logo.png"
            "DOGE" -> "https://cryptologos.cc/logos/dogecoin-doge-logo.png"
            else -> ""
        }
    }

    override suspend fun getAllAssets(): List<Asset> {
        try {
            Log.d("Firestore", "Attempting to fetch stocks from collection: ${stocksCollection.path}")
            val stocksSnapshot = stocksCollection.get().await()
            Log.d("Firestore", "Fetched ${stocksSnapshot.documents.size} stock documents")
            
            val stocks = stocksSnapshot.documents.mapNotNull { doc ->
                try {
                    Log.d("Firestore", "Processing stock document: ${doc.id} with data: ${doc.data}")
                    // Fetch details document for additional info
                    val detailsDoc = doc.reference.collection("description")
                        .document("details").get().await()
                    
                    val ticker = doc.getString("ticker") ?: return@mapNotNull null
                    TechStock(
                        ID = ticker,
                        name = detailsDoc.getString("name") ?: ticker,
                        price = doc.getDouble("price") ?: 0.0,
                        iconUrl = getStockIconUrl(ticker),
                        sector = detailsDoc.getString("industry") ?: "",
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        open = doc.getDouble("open") ?: 0.0,
                        previous_close = doc.getDouble("previous_close") ?: 0.0,
                        timestamp = doc.getString("timestamp") ?: "",
                        country = detailsDoc.getString("country") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("Firestore", "Error processing stock document: ${doc.id}", e)
                    null
                }
            }
            
            Log.d("Firestore", "Attempting to fetch crypto from collection: ${cryptoCollection.path}")
            val cryptoSnapshot = cryptoCollection.get().await()
            Log.d("Firestore", "Fetched ${cryptoSnapshot.documents.size} crypto documents")
            
            val crypto = cryptoSnapshot.documents.mapNotNull { doc ->
                try {
                    Log.d("Firestore", "Processing crypto document: ${doc.id} with data: ${doc.data}")
                    val symbol = doc.getString("symbol") ?: return@mapNotNull null
                    // Fetch details document for additional info
                    val detailsDoc = doc.reference.collection("description")
                        .document("details").get().await()
                    
                    Crypto(
                        ID = symbol,
                        name = detailsDoc.getString("name") ?: symbol,
                        price = doc.getDouble("price") ?: 0.0,
                        iconUrl = getCryptoIconUrl(symbol),
                        blockchain = detailsDoc.getString("blockchain") ?: "",
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        previous_close = doc.getDouble("previous_close") ?: 0.0,
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("Firestore", "Error processing crypto document: ${doc.id}", e)
                    null
                }
            }
            
            val result = stocks + crypto
            Log.d("Firestore", "Total assets fetched: ${result.size} (${stocks.size} stocks, ${crypto.size} crypto)")
            return result
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching assets", e)
            throw Exception("Failed to fetch assets: ${e.message}")
        }
    }

    override suspend fun findAsset(id: String): Asset {
        try {
            // Try finding in stocks first
            val stockDoc = stocksCollection.whereEqualTo("ticker", id).get().await().documents.firstOrNull()
            if (stockDoc != null) {
                // Fetch details document for additional info
                val detailsDoc = stockDoc.reference.collection("description")
                    .document("details").get().await()
                
                val ticker = stockDoc.getString("ticker") ?: throw Exception("Asset not found")
                return TechStock(
                    ID = ticker,
                    name = detailsDoc.getString("name") ?: ticker,
                    price = stockDoc.getDouble("price") ?: 0.0,
                    iconUrl = getStockIconUrl(ticker),
                    sector = detailsDoc.getString("industry") ?: "",
                    high = stockDoc.getDouble("high") ?: 0.0,
                    low = stockDoc.getDouble("low") ?: 0.0,
                    open = stockDoc.getDouble("open") ?: 0.0,
                    previous_close = stockDoc.getDouble("previous_close") ?: 0.0,
                    timestamp = stockDoc.getString("timestamp") ?: "",
                    country = detailsDoc.getString("country") ?: ""
                )
            }
            
            // If not found in stocks, try crypto
            val cryptoDoc = cryptoCollection.whereEqualTo("symbol", id).get().await().documents.firstOrNull()
            if (cryptoDoc != null) {
                val symbol = cryptoDoc.getString("symbol") ?: throw Exception("Asset not found")
                // Fetch details document for additional info
                val detailsDoc = cryptoDoc.reference.collection("description")
                    .document("details").get().await()
                
                return Crypto(
                    ID = symbol,
                    name = detailsDoc.getString("name") ?: symbol,
                    price = cryptoDoc.getDouble("price") ?: 0.0,
                    iconUrl = getCryptoIconUrl(symbol),
                    blockchain = detailsDoc.getString("blockchain") ?: "",
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

    override suspend fun filterAssetsByType(type: AssetType): List<Asset> {
        return when (type) {
            AssetType.STOCK -> stocksCollection.get().await().documents.mapNotNull { doc ->
                try {
                    val detailsDoc = doc.reference.collection("description")
                        .document("details").get().await()
                    
                    val ticker = doc.getString("ticker") ?: return@mapNotNull null
                    TechStock(
                        ID = ticker,
                        name = detailsDoc.getString("name") ?: ticker,
                        price = doc.getDouble("price") ?: 0.0,
                        iconUrl = getStockIconUrl(ticker),
                        sector = detailsDoc.getString("industry") ?: "",
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        open = doc.getDouble("open") ?: 0.0,
                        previous_close = doc.getDouble("previous_close") ?: 0.0,
                        timestamp = doc.getString("timestamp") ?: "",
                        country = detailsDoc.getString("country") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("Firestore", "Error processing stock document", e)
                    null
                }
            }
            AssetType.CRYPTO -> cryptoCollection.get().await().documents.mapNotNull { doc ->
                try {
                    val symbol = doc.getString("symbol") ?: return@mapNotNull null
                    val detailsDoc = doc.reference.collection("description")
                        .document("details").get().await()
                    
                    Crypto(
                        ID = symbol,
                        name = detailsDoc.getString("name") ?: symbol,
                        price = doc.getDouble("price") ?: 0.0,
                        iconUrl = getCryptoIconUrl(symbol),
                        blockchain = detailsDoc.getString("blockchain") ?: "",
                        high = doc.getDouble("high") ?: 0.0,
                        low = doc.getDouble("low") ?: 0.0,
                        previous_close = doc.getDouble("previous_close") ?: 0.0,
                        timestamp = doc.getString("timestamp") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("Firestore", "Error processing crypto document", e)
                    null
                }
            }
            AssetType.ALL -> getAllAssets()
        }
    }

    override suspend fun sortAssets(criteria: SortingCriteria): List<Asset> {
        TODO("Not yet implemented")
    }

    override suspend fun searchAssets(search: String): List<Asset> {
        try {
            val stocks = stocksCollection
                .whereGreaterThanOrEqualTo("ticker", search)
                .whereLessThanOrEqualTo("ticker", search + '\uf8ff')
                .get().await().documents.mapNotNull { doc ->
                    try {
                        // Fetch details document for additional info
                        val detailsDoc = doc.reference.collection("description")
                            .document("details").get().await()
                        
                        val ticker = doc.getString("ticker") ?: return@mapNotNull null
                        TechStock(
                            ID = ticker,
                            name = detailsDoc.getString("name") ?: ticker,
                            price = doc.getDouble("price") ?: 0.0,
                            iconUrl = getStockIconUrl(ticker),
                            sector = detailsDoc.getString("industry") ?: "",
                            high = doc.getDouble("high") ?: 0.0,
                            low = doc.getDouble("low") ?: 0.0,
                            open = doc.getDouble("open") ?: 0.0,
                            previous_close = doc.getDouble("previous_close") ?: 0.0,
                            timestamp = doc.getString("timestamp") ?: "",
                            country = detailsDoc.getString("country") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error processing stock document: ${doc.id}", e)
                        null
                    }
                }
                
            val crypto = cryptoCollection
                .whereGreaterThanOrEqualTo("symbol", search)
                .whereLessThanOrEqualTo("symbol", search + '\uf8ff')
                .get().await().documents.mapNotNull { doc ->
                    try {
                        val symbol = doc.getString("symbol") ?: return@mapNotNull null
                        // Fetch details document for additional info
                        val detailsDoc = doc.reference.collection("description")
                            .document("details").get().await()
                        
                        Crypto(
                            ID = symbol,
                            name = detailsDoc.getString("name") ?: symbol,
                            price = doc.getDouble("price") ?: 0.0,
                            iconUrl = getCryptoIconUrl(symbol),
                            blockchain = detailsDoc.getString("blockchain") ?: "",
                            high = doc.getDouble("high") ?: 0.0,
                            low = doc.getDouble("low") ?: 0.0,
                            previous_close = doc.getDouble("previous_close") ?: 0.0,
                            timestamp = doc.getString("timestamp") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error processing crypto document: ${doc.id}", e)
                        null
                    }
                }
                
            return stocks + crypto
        } catch (e: Exception) {
            throw Exception("Failed to search assets: ${e.message}")
        }
    }

    override suspend fun getYearlyHistoryPricePoints(id: String): List<Double> {
        try {
            val priceHistory = db.collection("priceHistory")
                .document(id)
                .collection("yearly")
                .get().await()
            
            return priceHistory.documents.mapNotNull { it.getDouble("price") }
        } catch (e: Exception) {
            throw Exception("Failed to fetch price history: ${e.message}")
        }
    }

    override suspend fun getDailyHistoryPricePoints(id: String): List<Double> {
        try {
            val priceHistory = db.collection("priceHistory")
                .document(id)
                .collection("daily")
                .get().await()
            
            return priceHistory.documents.mapNotNull { it.getDouble("price") }
        } catch (e: Exception) {
            throw Exception("Failed to fetch daily prices: ${e.message}")
        }
    }
} 