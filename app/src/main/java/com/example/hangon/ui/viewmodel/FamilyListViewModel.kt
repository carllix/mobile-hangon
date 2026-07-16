package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangon.data.model.FamilySummary
import com.example.hangon.data.repository.ApiResult
import com.example.hangon.data.repository.FamilyRepository
import com.example.hangon.data.repository.RetrofitFamilyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FamilyListUiState(
    val isLoading: Boolean = false,
    val families: List<FamilySummary> = emptyList(),
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val showJoinDialog: Boolean = false,
    val showCreateDialog: Boolean = false
)

class FamilyListViewModel(
    private val familyRepository: FamilyRepository = RetrofitFamilyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyListUiState())
    val uiState: StateFlow<FamilyListUiState> = _uiState.asStateFlow()

    init {
        loadFamilies()
    }

    fun loadFamilies() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = familyRepository.listMyFamilies()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(isLoading = false, families = result.data) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun onShowJoinDialog(show: Boolean) {
        _uiState.update { it.copy(showJoinDialog = show, errorMessage = null) }
    }

    fun onShowCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDialog = show, errorMessage = null) }
    }

    fun onCreateFamily(name: String) {
        if (name.isBlank()) return
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val result = familyRepository.createFamily(name)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, showCreateDialog = false) }
                    loadFamilies()
                }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
            }
        }
    }

    fun onJoinFamily(inviteCode: String) {
        if (inviteCode.isBlank()) return
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val result = familyRepository.joinFamily(inviteCode)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, showJoinDialog = false) }
                    loadFamilies()
                }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
            }
        }
    }
}
