package com.example.act22.viewmodel

import com.example.act22.data.model.Asset
import com.example.act22.data.model.Crypto
import com.example.act22.data.model.TechStock
import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.model.Portfolio
import com.example.act22.data.repository.FirestoreAssetRepository
import com.example.act22.data.repository.AssetRepositoryFirebaseImpl
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PortfolioViewModel(
    initialPortfolio: Portfolio = Portfolio(1, mutableListOf(), mutableListOf())
) : ViewModel() {
    private val portfolio: Portfolio = initialPortfolio
    private val allPortfolioAssets = mutableSetOf<Asset>()
    private val firestoreAssetRepository = FirestoreAssetRepository()
    private val assetRepository = AssetRepositoryFirebaseImpl()
    
    private val _portfolioState = MutableStateFlow<Portfolio>(portfolio)
    val portfolioState: StateFlow<Portfolio> = _portfolioState

    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance

    private val _cryptoList = MutableStateFlow<List<Crypto>>(emptyList())
    val cryptoList: StateFlow<List<Crypto>> = _cryptoList

    private val _stockList = MutableStateFlow<List<TechStock>>(emptyList())
    val stockList: StateFlow<List<TechStock>> = _stockList

    init {
        refreshPortfolio()
    }

    fun refreshPortfolio() {
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.email
                println("DEBUG: Starting portfolio refresh for user: $userId")
                
                if (userId != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userDoc = db.collection("androidUsers").whereEqualTo("email", userId).limit(1).get().await()
                    println("DEBUG: Found user document: ${!userDoc.isEmpty}")
                    
                    if (!userDoc.isEmpty) {
                        val userData = userDoc.documents[0]
                        println("DEBUG: User data retrieved")
                        
                        // Get wallet balance
                        _walletBalance.value = userData.getDouble("wallet_balance") ?: 0.0
                        println("DEBUG: Wallet balance: ${_walletBalance.value}")
                        
                        // Get assets
                        val assets = userData.get("assets") as? Map<String, Any>
                        println("DEBUG: Raw assets from Firestore: $assets")
                        
                        val stocksMap = (assets?.get("stock") as? Map<String, Any>) ?: mapOf()
                        val cryptosMap = (assets?.get("crypto") as? Map<String, Any>) ?: mapOf()
                        
                        println("DEBUG: Stocks in database: ${stocksMap.keys}")
                        println("DEBUG: Cryptos in database: ${cryptosMap.keys}")
                        
                        // Convert cryptos to Crypto objects
                        val cryptos = cryptosMap.mapNotNull { (symbol, quantity) ->
                            // Get current crypto price from crypto collection
                            val cryptoDoc = db.collection("cryptoCollection").document(symbol).get().await()
                            if (cryptoDoc.exists()) {
                                println("Found crypto: $symbol with quantity: $quantity")
                                Crypto(
                                    ID = symbol,
                                    name = symbol,
                                    price = cryptoDoc.getDouble("price") ?: 0.0,
                                    quantity = (quantity as? Number)?.toDouble() ?: 0.0,
                                    iconUrl = ""
                                )
                            } else {
                                println("Crypto not found in collection: $symbol")
                                null
                            }
                        }.filter { it.quantity > 0 }

                        // Convert stocks to TechStock objects
                        val stocks = stocksMap.mapNotNull { (symbol, quantity) ->
                            // Get current stock price from stocks collection
                            val stockDoc = db.collection("stocksCollection").document(symbol).get().await()
                            if (stockDoc.exists()) {
                                println("Found stock: $symbol with quantity: $quantity")
                                TechStock(
                                    ID = symbol,
                                    name = symbol,
                                    price = stockDoc.getDouble("price") ?: 0.0,
                                    quantity = (quantity as? Number)?.toDouble() ?: 0.0,
                                    iconUrl = ""
                                )
                            } else {
                                println("Stock not found in collection: $symbol")
                                null
                            }
                        }.filter { it.quantity > 0 }

                        // Update the LiveData values
                        _cryptoList.value = cryptos
                        _stockList.value = stocks
                        println("Updated portfolio with ${cryptos.size} cryptos and ${stocks.size} stocks")
                        
                        println("DEBUG: Final counts - Stocks: ${stocks.size}, Cryptos: ${cryptos.size}")
                        
                        val updatedPortfolio = Portfolio(1, stocks.toMutableList(), cryptos.toMutableList())
                        _portfolioState.value = updatedPortfolio
                        
                        // Update allPortfolioAssets
                        allPortfolioAssets.clear()
                        allPortfolioAssets.addAll(stocks)
                        allPortfolioAssets.addAll(cryptos)
                        println("DEBUG: Portfolio refresh complete. Total assets: ${allPortfolioAssets.size}")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error refreshing portfolio: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun isPortfolioEmpty(): Boolean {
        return allPortfolioAssets.isEmpty()
    }

    fun isAssetInPortfolio(asset: Asset): Boolean {
        return allPortfolioAssets.any { it.ID == asset.ID }
    }

    fun getAssetQuantity(assetId: String): Double {
        return allPortfolioAssets.find { it.ID == assetId }?.quantity ?: 0.0
    }

    fun getPortfolio(): Portfolio {
        return _portfolioState.value
    }

    suspend fun buyAsset(asset: Asset, quantity: Double, onComplete: (Boolean, String) -> Unit) {
        try {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.email
            if (userId == null) {
                onComplete(false, "User not logged in")
                return
            }

            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("androidUsers").whereEqualTo("email", userId).limit(1).get().await()
            
            if (userDoc.isEmpty) {
                onComplete(false, "User not found")
                return
            }

            val userData = userDoc.documents[0]
            val currentBalance = userData.getDouble("wallet_balance") ?: 0.0
            val totalCost = quantity * asset.price

            if (totalCost > currentBalance) {
                onComplete(false, "Insufficient funds")
                return
            }

            // Update user document
            val userRef = userData.reference
            val assetType = if (asset is Crypto) "crypto" else "stock"
            val currentQuantity = getAssetQuantity(asset.ID)
            
            // Check portfolio limits
            val currentPortfolio = getPortfolio()
            if (assetType == "crypto" && !isAssetInPortfolio(asset) && currentPortfolio.cryptos.size >= 3) {
                onComplete(false, "You can only hold up to 3 different cryptocurrencies")
                return
            }
            if (assetType == "stock" && !isAssetInPortfolio(asset) && currentPortfolio.techStocks.size >= 10) {
                onComplete(false, "You can only hold up to 10 different stocks")
                return
            }
            
            println("Buying $quantity of $assetType ${asset.ID} at price ${asset.price}")
            println("Total cost: $totalCost")
            println("Current balance: $currentBalance")
            println("New balance will be: ${currentBalance - totalCost}")
            
            db.runTransaction { transaction ->
                // Update wallet balance
                transaction.update(userRef, "wallet_balance", currentBalance - totalCost)
                
                // Update asset quantity
                transaction.update(userRef, "assets.$assetType.${asset.ID}", currentQuantity + quantity)
            }.await()

            println("Transaction completed. Refreshing portfolio...")
            refreshPortfolio()
            onComplete(true, "Purchase successful")
        } catch (e: Exception) {
            println("Error during purchase: ${e.message}")
            onComplete(false, "Error: ${e.message}")
        }
    }

    suspend fun sellAsset(asset: Asset, quantity: Double, onComplete: (Boolean, String) -> Unit) {
        try {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.email
            if (userId == null) {
                onComplete(false, "User not logged in")
                return
            }

            val currentQuantity = getAssetQuantity(asset.ID)
            if (quantity > currentQuantity) {
                onComplete(false, "Cannot sell more than owned quantity (${currentQuantity})")
                return
            }

            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("androidUsers").whereEqualTo("email", userId).limit(1).get().await()
            
            if (userDoc.isEmpty) {
                onComplete(false, "User not found")
                return
            }

            val userData = userDoc.documents[0]
            val currentBalance = userData.getDouble("wallet_balance") ?: 0.0
            val totalValue = quantity * asset.price

            println("Selling asset: ${asset.name}")
            println("Current balance: $currentBalance")
            println("Sale value: $totalValue")
            println("New balance will be: ${currentBalance + totalValue}")

            // Update user document
            val userRef = userData.reference
            val assetType = if (asset is Crypto) "crypto" else "stock"
            
            db.runTransaction { transaction ->
                // Update wallet balance
                transaction.update(userRef, "wallet_balance", currentBalance + totalValue)
                
                // Update asset quantity
                val newQuantity = currentQuantity - quantity
                if (newQuantity > 0) {
                    transaction.update(userRef, "assets.$assetType.${asset.ID}", newQuantity)
                } else {
                    // Remove the asset if quantity becomes 0
                    transaction.update(userRef, "assets.$assetType.${asset.ID}", null)
                }
            }.await()

            println("Transaction completed. Refreshing portfolio...")
            refreshPortfolio()
            onComplete(true, "Sale successful - Added $${String.format("%.2f", totalValue)} to wallet")
        } catch (e: Exception) {
            println("Error during sale: ${e.message}")
            onComplete(false, "Error: ${e.message}")
        }
    }
}