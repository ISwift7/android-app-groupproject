package com.example.act22.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.service.WalletService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val walletService = WalletService()

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun refreshBalance(onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        viewModelScope.launch {
                            walletService.getBalance(idToken!!)
                                .onSuccess { newBalance ->
                                    _balance.value = newBalance
                                }
                                .onFailure { e ->
                                    onError(e.message ?: "Failed to get balance")
                                }
                        }
                    } else {
                        onError("Failed to get authentication token")
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPayment(amount: Double, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        viewModelScope.launch {
                            walletService.createPaymentIntent(amount, idToken!!)
                                .onSuccess { clientSecret ->
                                    // Automatically confirm payment
                                    confirmPayment(clientSecret, amount, onSuccess = {
                                        onSuccess(clientSecret)
                                    }, onError = onError)
                                }
                                .onFailure { e ->
                                    onError(e.message ?: "Failed to create payment")
                                }
                        }
                    } else {
                        onError("Failed to get authentication token")
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun confirmPayment(clientSecret: String, amount: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        viewModelScope.launch {
                            walletService.confirmPayment(clientSecret, amount, idToken!!)
                                .onSuccess { newBalance ->
                                    _balance.value = newBalance
                                    onSuccess()
                                }
                                .onFailure { e ->
                                    onError(e.message ?: "Failed to confirm payment")
                                }
                        }
                    } else {
                        onError("Failed to get authentication token")
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to confirm payment")
            }
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
} 