package com.example.act22.viewmodel

import androidx.lifecycle.ViewModel
import com.example.act22.data.model.Asset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserPlanViewModel(

): ViewModel() {
    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val plan: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun getUserPlan(){

    }

    fun changeUserPlan(newPlan: String){

    }
}