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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

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
    private var currentAssetId: String? = null
    private var currentIsCrypto: Boolean = false
    private var lastFetchTime: Long = 0
    private val minFetchInterval = 2000 // Minimum 2 seconds between fetches

    fun clearChartState() {
        _chartUiState.value = ChartUiState.Loading
        currentAssetId = null
        lastFetchTime = 0
    }

    fun fetchAssetInformation(id: String, isCrypto: Boolean, shouldUpdateGraph: Boolean = true) {
        if (id != currentAssetId) {
            clearChartState()
            currentAssetId = id
            currentIsCrypto = isCrypto
            
            // Immediately fetch both asset and graph data in parallel
            viewModelScope.launch {
                supervisorScope {
                    launch { fetchAsset(id) }
                    if (shouldUpdateGraph) {
                        launch { fetchGraphData(id, isCrypto) }
                    }
                }
            }
        } else {
            viewModelScope.launch {
                fetchAsset(id)
                if (shouldUpdateGraph) {
                    fetchGraphData(id, isCrypto)
                }
            }
        }
    }

    fun startGraphUpdates(id: String, isCrypto: Boolean) {
        if (id != currentAssetId) {
            clearChartState()
            currentAssetId = id
            currentIsCrypto = isCrypto
            
            // Immediate fetch for new asset
            viewModelScope.launch {
                fetchGraphData(id, isCrypto)
            }
        }

        stopGraphUpdates()

        graphUpdateJob = viewModelScope.launch {
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFetchTime >= minFetchInterval) {
                    fetchGraphData(id, isCrypto)
                }
                delay(3000) // Check every 3 seconds
            }
        }
    }

    fun stopGraphUpdates() {
        graphUpdateJob?.cancel()
        graphUpdateJob = null
    }

    private suspend fun fetchAsset(id: String) {
        try {
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
        if (System.currentTimeMillis() - lastFetchTime < minFetchInterval) {
            return
        }
        
        try {
            withContext(Dispatchers.IO) {
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
                    if (points != null) {
                        val recentPoints = points.take(14).reversed()
                        if (recentPoints.isNotEmpty()) {
                            _chartUiState.value = ChartUiState.Success(recentPoints)
                            lastFetchTime = System.currentTimeMillis()
                        } else {
                            _chartUiState.value = ChartUiState.Loading
                        }
                    } else {
                        _chartUiState.value = ChartUiState.Loading
                    }
                } else {
                    _chartUiState.value = ChartUiState.Loading
                }
            }
        } catch (e: Exception) {
            Log.e("AssetPriceViewModel", "Failed to load graph", e)
            _chartUiState.value = ChartUiState.Loading
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGraphUpdates()
        clearChartState()
    }
}
