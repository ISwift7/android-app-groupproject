package com.example.act22.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.model.Asset
import com.example.act22.network.RetrofitInstance
import com.example.act22.network.PurchaseRequest
import com.example.act22.network.SellRequest
import com.example.act22.network.TradingResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response

sealed class TradingState {
    object Idle : TradingState()
    object Loading : TradingState()
    data class Success(val message: String, val newBalance: Double) : TradingState()
    data class Error(val message: String) : TradingState()
}

class TradingViewModel : ViewModel() {
    private val _tradingState = MutableStateFlow<TradingState>(TradingState.Idle)
    val tradingState: StateFlow<TradingState> = _tradingState

    fun setError(message: String) {
        _tradingState.value = TradingState.Error(message)
    }

    fun buyAsset(symbol: String, isCrypto: Boolean, quantity: Double, currentPrice: Double) {
        viewModelScope.launch {
            _tradingState.value = TradingState.Loading
            try {
                val token = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
                    ?: throw Exception("Not authenticated")

                val totalCost = quantity * currentPrice
                val request = PurchaseRequest(
                    symbol = symbol,
                    is_crypto = isCrypto,
                    quantity = quantity,
                    price_per_unit = currentPrice,
                    total_cost = totalCost
                )

                val response = RetrofitInstance.tradingApi.purchaseAsset(
                    token = "Bearer $token",
                    request = request
                )

                handleResponse(response)
            } catch (e: Exception) {
                _tradingState.value = TradingState.Error(e.message ?: "Failed to buy asset")
            }
        }
    }

    fun sellAsset(symbol: String, isCrypto: Boolean, quantity: Double, currentPrice: Double) {
        viewModelScope.launch {
            _tradingState.value = TradingState.Loading
            try {
                val token = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
                    ?: throw Exception("Not authenticated")

                val totalValue = quantity * currentPrice
                val request = SellRequest(
                    symbol = symbol,
                    is_crypto = isCrypto,
                    quantity = quantity,
                    price_per_unit = currentPrice,
                    total_value = totalValue
                )

                val response = RetrofitInstance.tradingApi.sellAsset(
                    token = "Bearer $token",
                    request = request
                )

                handleResponse(response)
            } catch (e: Exception) {
                _tradingState.value = TradingState.Error(e.message ?: "Failed to sell asset")
            }
        }
    }

    private fun handleResponse(response: Response<TradingResponse>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                _tradingState.value = TradingState.Success(
                    message = body.message,
                    newBalance = body.transaction.new_balance
                )
            } else {
                _tradingState.value = TradingState.Error("Empty response from server")
            }
        } else {
            _tradingState.value = TradingState.Error(response.errorBody()?.string() ?: "Transaction failed")
        }
    }
}
