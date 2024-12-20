package com.example.act22.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.repository.AiRepository
import com.example.act22.data.repository.AiRepositoryTestImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

//todo specify dispacher
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

    fun fetchAssetAnalysis(assetId: String) {
        _analysisState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.fetchAssetAnalysis(assetId)
                _analysisState.value = UiState.Success(listOf(result))
            } catch (e: Exception) {
                _analysisState.value = UiState.Error("Error fetching analysis: ${e.message}")
            }
        }
    }

    fun fetchAssetPricePrediction(assetId: String) {
        _predictionState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.fetchAssetPricePrediction(assetId)
                _predictionState.value = UiState.Success(result)
            } catch (e: Exception) {
                _predictionState.value = UiState.Error("Error fetching predictions: ${e.message}")
            }
        }
    }

    fun fetchPortfolioRecommendation() {
        _recommendationState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.fetchPortfolioRecommendation()
                _recommendationState.value = UiState.Success(result)
            } catch (e: Exception) {
                _recommendationState.value = UiState.Error("Error fetching recommendations: ${e.message}")
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
