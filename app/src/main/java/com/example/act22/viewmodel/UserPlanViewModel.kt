package com.example.act22.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.repository.UserRepository
import com.example.act22.service.WalletService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserPlanViewModel() : ViewModel() {
    private val repository = UserRepository()
    private val walletService: WalletService = WalletService()

    private var _userPlan = MutableStateFlow<Boolean>(false)
    val userPlan: StateFlow<Boolean> = _userPlan

    private var wasPremiumPayedBefore = false

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init{
        fetchUserPlanInfo()
    }

    fun fetchUserPlanInfo() {
        viewModelScope.launch {
            try {
                val isPremium = repository.isPremiumUser()
                _userPlan.value = isPremium
                wasPremiumPayedBefore = repository.hasUserPayedBefore()
            } catch (e: Exception) {
                println("Error getting plan info: ${e.message}")
                _errorMessage.value = "Failed to fetch user plan info."
            }
        }
    }

    fun changeUserPlan(newPlan: String) {
        val isUpgradingToPremium = newPlan == "Premium"
        viewModelScope.launch {
            try {
                if (isUpgradingToPremium) {
                    if (!wasPremiumPayedBefore) {
                        processPayment(
                            amount = 50.0,
                            onSuccess = {
                                viewModelScope.launch {
                                    try {
                                        repository.updateUserPlan(true)
                                        repository.recordPayment()
                                        _userPlan.value = true
                                        wasPremiumPayedBefore = true
                                        _errorMessage.value = "You are premium user now!"
                                    } catch (e: Exception) {
                                        _errorMessage.value = "Failed to update plan in database: ${e.message}"
                                    }
                                }
                            },
                            onError = { error ->
                                _errorMessage.value = error
                            }
                        )
                    } else {
                        try {
                            repository.updateUserPlan(true)
                            _userPlan.value = true
                            _errorMessage.value = "You are now premium user again!"
                        } catch (e: Exception) {
                            _errorMessage.value = "Failed to update plan in database: ${e.message}"
                        }
                    }
                } else {
                    try {
                        repository.updateUserPlan(false)
                        _userPlan.value = false
                        _errorMessage.value = "You have downgraded to lite user!"
                    } catch (e: Exception) {
                        _errorMessage.value = "Failed to update plan in database: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error changing plan: ${e.message}"
            }
        }
    }


    private suspend fun processPayment(amount: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            val idToken = FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()?.token
                ?: throw Exception("Failed to get authentication token")

            walletService.withdrawFunds(amount, idToken)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { error ->
                    onError("Failed to withdraw funds: ${error.message}")
                }
        } catch (e: Exception) {
            onError("Failed to process payment: ${e.message}")
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

}