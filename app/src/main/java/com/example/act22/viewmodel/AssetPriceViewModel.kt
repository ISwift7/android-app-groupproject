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

    fun fetchAssetInformation(id: String, isCrypto: Boolean, shouldUpdateGraph: Boolean = true) {
        viewModelScope.launch {
            fetchAsset(id)
            if (shouldUpdateGraph) {
                fetchGraphData(id, isCrypto)
            }
        }
    }

    fun startGraphUpdates(id: String, isCrypto: Boolean) {
        stopGraphUpdates()

        graphUpdateJob = viewModelScope.launch {
            while (true) {
                fetchGraphData(id, isCrypto)
                delay(300000) // Update every 5 minutes
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
            // Get Firebase auth token
            val user = FirebaseAuth.getInstance().currentUser
            val token = user?.getIdToken(false)?.await()?.token
                ?: throw Exception("Not authenticated")

            // Make the API call
            Log.d("FetchGraphData", "Making API call for graph data...")
            val response = RetrofitInstance.graphApi.getAssetGraph(
                token = "Bearer $token",
                symbol = id,
                isCrypto = isCrypto
            )

            if (response.isSuccessful) {
                val graphData = response.body()?.data
                if (graphData != null) {
                    _chartUiState.value = ChartUiState.Success(graphData.points)
                } else {
                    _chartUiState.value = ChartUiState.Error("No graph data available")
                }
            } else {
                _chartUiState.value = ChartUiState.Error("Failed to fetch graph data")
            }
        } catch (e: Exception) {
            _chartUiState.value = ChartUiState.Error("Failed to load graph: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGraphUpdates()
    }
}
