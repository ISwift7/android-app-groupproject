package com.example.act22.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.model.Asset
import com.example.act22.data.repository.AssetRepository
import com.example.act22.data.repository.AssetRepositoryTestImp
import com.example.act22.network.GraphDataPoint
import com.example.act22.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Job

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
        data class Success(val points: List<GraphDataPoint>) : ChartUiState()
        data class Error(val message: String) : ChartUiState()
    }

    private val _assetUiState = MutableStateFlow<AssetUiState>(AssetUiState.Loading)
    val assetUiState: StateFlow<AssetUiState> = _assetUiState

    private val _chartUiState = MutableStateFlow<ChartUiState>(ChartUiState.Loading)
    val chartUiState: StateFlow<ChartUiState> = _chartUiState

    private var graphUpdateJob: Job? = null

    fun clearChartState() {
        _chartUiState.value = ChartUiState.Loading
    }

    fun fetchAssetInformation(id: String, isCrypto: Boolean, shouldUpdateGraph: Boolean = true) {
        viewModelScope.launch {
            // Clear previous states
            _assetUiState.value = AssetUiState.Loading
            clearChartState()
            
            fetchAsset(id)
            if (shouldUpdateGraph) {
                fetchGraphData(id, isCrypto)
            }
        }
    }

    fun startGraphUpdates(id: String, isCrypto: Boolean) {
        stopGraphUpdates()
        clearChartState()

        graphUpdateJob = viewModelScope.launch {
            while (true) {
                fetchGraphData(id, isCrypto)
                delay(10000) // Update every 10 seconds instead of 5 minutes for more responsive updates
            }
        }
    }

    fun stopGraphUpdates() {
        graphUpdateJob?.cancel()
        graphUpdateJob = null
    }

    private suspend fun fetchAsset(id: String) {
        try {
            _assetUiState.value = AssetUiState.Loading
            val asset = repository.findAsset(id)
            if (asset.price <= 0) {
                _assetUiState.value = AssetUiState.Error("Could not get current price")
            } else {
                _assetUiState.value = AssetUiState.Success(asset)
            }
        } catch (e: Exception) {
            _assetUiState.value = AssetUiState.Error("Failed to load asset: ${e.message}")
        }
    }

    private suspend fun fetchGraphData(id: String, isCrypto: Boolean) {
        try {
            // Don't set loading state for updates to avoid flickering
            if (_chartUiState.value is ChartUiState.Loading) {
                _chartUiState.value = ChartUiState.Loading
            }
            
            // Get Firebase auth token
            val user = FirebaseAuth.getInstance().currentUser
            val token = user?.getIdToken(false)?.await()?.token
                ?: throw Exception("Not authenticated")

            val response = RetrofitInstance.graphApi.getAssetGraph(
                token = "Bearer $token",
                symbol = id,
                type = if (isCrypto) "cryptos" else "stocks"
            )

            if (response.isSuccessful) {
                val points = response.body()
                Log.d("AssetPriceViewModel", "Response successful. Points: $points")
                if (points != null) {
                    // Take only the 14 most recent points, reversed to show oldest to newest (left to right)
                    val recentPoints = points.take(14).reversed()
                    Log.d("AssetPriceViewModel", "Recent points size: ${recentPoints.size}")
                    if (recentPoints.isNotEmpty()) {
                        _chartUiState.value = ChartUiState.Success(recentPoints)
                    } else {
                        // Keep previous state or loading instead of showing error
                        if (_chartUiState.value !is ChartUiState.Success) {
                            _chartUiState.value = ChartUiState.Loading
                        }
                    }
                } else {
                    // Keep previous state or loading instead of showing error
                    if (_chartUiState.value !is ChartUiState.Success) {
                        _chartUiState.value = ChartUiState.Loading
                    }
                }
            } else {
                Log.e("AssetPriceViewModel", "Response not successful: ${response.errorBody()?.string()}")
                // Keep previous state or loading instead of showing error
                if (_chartUiState.value !is ChartUiState.Success) {
                    _chartUiState.value = ChartUiState.Loading
                }
            }
        } catch (e: Exception) {
            Log.e("AssetPriceViewModel", "Failed to load graph", e)
            // Keep previous state or loading instead of showing error
            if (_chartUiState.value !is ChartUiState.Success) {
                _chartUiState.value = ChartUiState.Loading
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGraphUpdates()
    }
}
