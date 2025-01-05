package com.example.act22.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.model.Portfolio
import com.example.act22.data.repository.AiRepository
import com.example.act22.data.repository.AiRepositoryTestImp
import com.example.act22.network.AnalyzeSingleRequest
import com.example.act22.network.PortfolioRecomandationsRequest
import com.example.act22.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

class AIViewModel(
    private val repository: AiRepository = AiRepositoryTestImp()
) : ViewModel() {
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val data: List<String>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _analysisState = MutableStateFlow<UiState>(UiState.Idle)
    val analysisState: StateFlow<UiState> = _analysisState

    private val _predictionState = MutableStateFlow<UiState>(UiState.Idle)
    val predictionState: StateFlow<UiState> = _predictionState

    private val _recommendationState = MutableStateFlow<UiState>(UiState.Idle)
    val recommendationState: StateFlow<UiState> = _recommendationState

    fun analyzeAsset(assetSymbol: String) {
        _predictionState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val result = RetrofitInstance.aiApi.analyzeSingleAsset(AnalyzeSingleRequest(assetSymbol))
                Log.e("AI", result.toString())
                if (result.isSuccessful && result.body()?.success == true) {
                    val results = result.body()?.results ?: emptyList()
                    val processedResults = mutableListOf<String>()

                    results.forEach { response ->
                        val splitResponse = response.split("\n")
                        if (splitResponse.size >= 2 && (splitResponse[1].equals("Buy", true) ||
                                    splitResponse[1].equals("Sell", true) ||
                                    splitResponse[1].equals("Hold", true))
                        ) {
                            // Keep everything after the first string as separate strings
                            processedResults.addAll(splitResponse.drop(1))
                        } else {
                            // Keep the entire response if structure is different
                            processedResults.add(response)
                        }
                    }

                    _predictionState.value = UiState.Success(processedResults)
                } else {
                    _predictionState.value = UiState.Error("Failed to analyze asset: ${result.message()}")
                }
            } catch (e: Exception) {
                _predictionState.value = UiState.Error("Error connecting to AI: ${e.message}")
            }
        }
    }

    private fun getAllAssetIDs(portfolio: Portfolio): List<String> {
        return portfolio.techStocks.map { it.ID } + portfolio.cryptos.map { it.ID }
    }

    fun portfolioRecommendations(portfolio: Portfolio) {
        _recommendationState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val allAssetIDs = getAllAssetIDs(portfolio)

                val result = RetrofitInstance.aiApi.getPortfolioRecomendations(
                    PortfolioRecomandationsRequest(allAssetIDs)
                )
                Log.e("AI", result.toString())
                if (result.isSuccessful && result.body()?.success == true) {
                    val recommendations = result.body()?.recomendations ?: emptyList()
                    _recommendationState.value = UiState.Success(recommendations)
                } else {
                    _recommendationState.value = UiState.Error("Failed to analyze portfolio: ${result.message()}")
                }
            } catch (e: Exception) {
                _recommendationState.value = UiState.Error("Error connecting to AI: ${e.message}")
            }
        }
    }

    fun resetAnalysisState() {
        _analysisState.value = UiState.Idle
    }

    fun resetPredictionState() {
        _predictionState.value = UiState.Idle
    }

    fun resetRecommendationState() {
        _recommendationState.value = UiState.Idle
    }
}
