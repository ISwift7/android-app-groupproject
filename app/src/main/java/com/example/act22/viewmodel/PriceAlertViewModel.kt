package com.example.act22.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.model.Asset
import com.example.act22.data.model.Crypto
import com.example.act22.data.model.PriceAlert
import com.example.act22.data.repository.PriceAlertRepository
import com.example.act22.data.repository.PriceAlertRepositoryFirebaseImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PriceAlertViewModel(
    private val repository: PriceAlertRepository = PriceAlertRepositoryFirebaseImpl()
) : ViewModel() {
    private val _priceAlerts = MutableStateFlow<List<PriceAlert>>(emptyList())
    val priceAlerts: StateFlow<List<PriceAlert>> = _priceAlerts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchPriceAlertsForAsset(asset: Asset) {
        viewModelScope.launch {
            try {
                _priceAlerts.value = repository.getPriceAlertsForAsset(asset)
            } catch (e: Exception) {
                _error.value = "Failed to fetch price alerts: ${e.message}"
            }
        }
    }

    fun addPriceAlert(asset: Asset, targetPrice: Double, alertType: String) {
        viewModelScope.launch {
            try {
                // Validate alert type
                if (alertType !in listOf("above", "below")) {
                    _error.value = "Invalid alert type. Must be 'above' or 'below'"
                    return@launch
                }

                // Check if we already have 3 active alerts
                val currentAlerts = repository.getPriceAlertsForAsset(asset)
                if (currentAlerts.size >= 3) {
                    _error.value = "You can only have up to 3 active price alerts"
                    return@launch
                }

                val alert = PriceAlert(
                    symbol = asset.ID,
                    is_crypto = asset is Crypto,
                    target_price = targetPrice,
                    alert_type = alertType,
                    current_price_at_creation = asset.price,
                    created_at = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )

                repository.addPriceAlert(alert)
                fetchPriceAlertsForAsset(asset)
            } catch (e: Exception) {
                _error.value = "Failed to add price alert: ${e.message}"
            }
        }
    }

    fun deletePriceAlert(alert: PriceAlert, asset: Asset) {
        viewModelScope.launch {
            try {
                repository.deletePriceAlert(alert)
                fetchPriceAlertsForAsset(asset)
            } catch (e: Exception) {
                _error.value = "Failed to delete price alert: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}