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

    fun validateAndProcessPayment(
        amount: String,
        cardNumber: String,
        expiryDate: String,
        cvv: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null) {
            onError("Please enter a valid amount")
            return
        }

        val cardDigits = cardNumber.filter { it.isDigit() }
        if (cardDigits.length != 16) {
            onError("Please enter a valid card number")
            return
        }

        if (cvv.length != 3) {
            onError("Please enter a valid CVV")
            return
        }

        val expiryDigits = expiryDate.filter { it.isDigit() }
        if (expiryDigits.length != 4) {
            onError("Please enter expiry date in MM/YY format")
            return
        }

        val monthStr = expiryDigits.take(2)
        val yearStr = expiryDigits.drop(2)
        val expiryMonth = monthStr.toIntOrNull()
        val expiryYear = yearStr.toIntOrNull()?.let { 2000 + it }

        if (expiryMonth == null || expiryYear == null || expiryMonth !in 1..12) {
            onError("Please enter a valid expiry date")
            return
        }

        viewModelScope.launch {
            try {
                setLoading(true)
                createPayment(
                    amount = amountValue,
                    onSuccess = { clientSecret ->
                        onSuccess(clientSecret)
                        refreshBalance { error ->
                            if (error != null) {
                                onError(error)
                            }
                        }
                    },
                    onError = { error ->
                        onError(error)
                    }
                )
            } catch (e: Exception) {
                onError("Network error: Please check your connection")
            } finally {
                setLoading(false)
            }
        }
    }
} 