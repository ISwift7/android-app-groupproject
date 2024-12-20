package com.example.act22.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.model.Asset
import com.example.act22.data.repository.AssetRepository
import com.example.act22.data.repository.AssetRepositoryTestImp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AssetPriceViewModel(
    private val repository: AssetRepository = AssetRepositoryTestImp()
) : ViewModel() {

    sealed class AssetUiState {
        object Loading : AssetUiState()
        data class Success(val asset: Asset) : AssetUiState()
        data class Error(val message: String) : AssetUiState()
    }

    sealed class ChartUiState {
        object Loading : ChartUiState()
        data class Success(val pricePoints: List<Double>) : ChartUiState()
        data class Error(val message: String) : ChartUiState()
    }

    private val _assetUiState = MutableStateFlow<AssetUiState>(AssetUiState.Loading)
    val assetUiState: StateFlow<AssetUiState> = _assetUiState

    private val _chartUiState = MutableStateFlow<ChartUiState>(ChartUiState.Loading)
    val chartUiState: StateFlow<ChartUiState> = _chartUiState

    fun fetchAssetInformation(id: String){
        viewModelScope.launch {
            fetchAsset(id)
            fetchYearlyHistoryPricePoints(id)
        }
    }

    private  suspend fun fetchAsset(id: String) {
        try {
            _assetUiState.value = AssetUiState.Loading
            val asset = repository.findAsset(id)
            _assetUiState.value = AssetUiState.Success(asset)
        } catch (e: Exception) {
            _assetUiState.value = AssetUiState.Error("Failed to load asset: ${e.message}")
        }
    }

    suspend fun fetchYearlyHistoryPricePoints(id: String) {
        try {
            _chartUiState.value = ChartUiState.Loading
            val pricePoints = repository.getYearlyHistoryPricePoints(id)
            _chartUiState.value = ChartUiState.Success(pricePoints)
        } catch (e: Exception) {
            _chartUiState.value = ChartUiState.Error("Failed to load chart data: ${e.message}")
        }
    }

    suspend fun fetchDailyHistoryPricePoints(id: String) {
        try {
            _chartUiState.value = ChartUiState.Loading
            val pricePoints = repository.getYearlyHistoryPricePoints(id)
            _chartUiState.value = ChartUiState.Success(pricePoints)
        } catch (e: Exception) {
            _chartUiState.value = ChartUiState.Error("Failed to load chart data: ${e.message}")
        }
    }
}
