package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CallOverlayUiState(
    val callerNumber: String = "+1 2345 678 90"
)

class CallOverlayViewModel(callerNumber: String) : ViewModel() {

    private val _uiState = MutableStateFlow(CallOverlayUiState(callerNumber = callerNumber))
    val uiState: StateFlow<CallOverlayUiState> = _uiState.asStateFlow()

    fun onAcceptWithHangOn() {
        // In production: start HangOn monitoring service.
    }

    fun onSkip() {
        // In production: log the skip / dismissal.
    }
}

class CallOverlayViewModelFactory(private val callerNumber: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CallOverlayViewModel(callerNumber) as T
    }
}
