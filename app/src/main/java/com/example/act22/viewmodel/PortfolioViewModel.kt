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

class PortfolioViewModel(
    initialPortfolio: Portfolio = Portfolio(1, mutableListOf(), mutableListOf())
) : ViewModel(){
    private val portfolio: Portfolio = initialPortfolio
    private val allPortfolioAssets = mutableSetOf<Asset>()
    private val firestoreAssetRepository = FirestoreAssetRepository()
    private val assetRepository = AssetRepositoryFirebaseImpl()
    
    private val _portfolioState = MutableStateFlow<Portfolio>(portfolio)
    val portfolioState: StateFlow<Portfolio> = _portfolioState

    init {
        allPortfolioAssets.addAll(portfolio.cryptos)
        allPortfolioAssets.addAll(portfolio.techStocks)
    }

    fun isPortfolioEmpty(): Boolean {
        return allPortfolioAssets.isEmpty()
    }

    fun isAssetInPortfolio(asset: Asset):Boolean{
        return allPortfolioAssets.contains(asset)
    }

    suspend fun findAssetByName(name: String): Asset? {
        return try {
            val assets = assetRepository.searchAssets(name)
            assets.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun getPortfolio(): Portfolio {
        return portfolio
    }

    fun toggleAsset(asset: Asset, onResult: (String?) -> Unit) {
        if (allPortfolioAssets.contains(asset)) {
            allPortfolioAssets.remove(asset)
            removeAssetFromPortfolio(asset)
        } else {
            allPortfolioAssets.add(asset)
            addAssetToPortfolio(asset) { errorMessage ->
                if (errorMessage != null) {
                    allPortfolioAssets.remove(asset)
                    onResult(errorMessage)
                }
            }
        }
    }

    fun addAssetToPortfolio(asset: Asset, onResult: (String?) -> Unit){
        if(asset is Crypto){
            if(portfolio.cryptos.size >= 3)
                onResult("Maximum number of cryptos have been added.")
            else
                portfolio.cryptos.add(asset as Crypto)
        } else {
            if(portfolio.techStocks.size >= 10)
                onResult("Maximum number of tech stocks have been added.")
            else
                portfolio.techStocks.add(asset as TechStock)
        }
    }

    fun removeAssetFromPortfolio(asset: Asset){
        if(asset is Crypto) portfolio.cryptos.remove(asset as Crypto) else portfolio.techStocks.remove(asset as TechStock)
    }

    @SuppressLint("DefaultLocale")
    fun formatLargeNumber(number: Double): String {
        return when {
            number >= 1_000_000_000 -> String.format("%.1fB", number / 1_000_000_000) // Billions
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000) // Millions
            else -> String.format("%.2f", number) // Less than 1 million, display normally
        }
    }

    fun updateAssetPrices() {
        viewModelScope.launch {
            portfolio.techStocks.forEach { stock ->
                val updatedStock = firestoreAssetRepository.getStockData(stock.ID)
                if (updatedStock != null) {
                    stock.price = updatedStock.price
                    // Update other fields if needed
                }
            }
            
            portfolio.cryptos.forEach { crypto ->
                val updatedCrypto = firestoreAssetRepository.getCryptoData(crypto.ID)
                if (updatedCrypto != null) {
                    crypto.price = updatedCrypto.price
                    // Update other fields if needed
                }
            }
            
            _portfolioState.value = portfolio
        }
    }
}