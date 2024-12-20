package com.example.act22.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.model.Asset
import com.example.act22.data.model.PriceAlert
import com.example.act22.data.repository.PriceAlertRepository
import com.example.act22.data.repository.PriceAlertRepositoryTestImp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PriceAlertViewModel(
    private val repository: PriceAlertRepository = PriceAlertRepositoryTestImp()
): ViewModel() {
    private val _priceAlerts = MutableStateFlow<List<PriceAlert>>(emptyList())
    val priceAlerts: StateFlow<List<PriceAlert>> = _priceAlerts

    fun fetchPriceAlertsForAsset(asset: Asset) {
        viewModelScope.launch {
            _priceAlerts.value = repository.getPriceAlertsForAsset(asset)
        }
    }

    fun addPriceAlert(asset: Asset, price: Double) {
        viewModelScope.launch {
            val alert = PriceAlert(asset,"today lol", price)
            repository.addPriceAlert(alert)
            fetchPriceAlertsForAsset(alert.asset)
        }
    }

    fun deletePriceAlert(alert: PriceAlert) {
        viewModelScope.launch {
            repository.deletePriceAlert(alert)
            fetchPriceAlertsForAsset(alert.asset)
        }
    }
}