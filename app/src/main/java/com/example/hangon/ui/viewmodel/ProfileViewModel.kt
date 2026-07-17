package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangon.data.model.UserProfile
import com.example.hangon.data.repository.ApiResult
import com.example.hangon.data.repository.ProfileRepository
import com.example.hangon.data.repository.RetrofitProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val isEditingName: Boolean = false,
    val nameInput: String = "",
    val isSavingName: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val profileRepository: ProfileRepository = RetrofitProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = profileRepository.getProfile()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(isLoading = false, profile = result.data) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun onEditNameClick() {
        val currentName = _uiState.value.profile?.displayName.orEmpty()
        _uiState.update { it.copy(isEditingName = true, nameInput = currentName, errorMessage = null) }
    }

    fun onCancelEditName() {
        _uiState.update { it.copy(isEditingName = false, nameInput = "") }
    }

    fun onNameInputChange(value: String) {
        _uiState.update { it.copy(nameInput = value) }
    }

    fun onSaveName() {
        val name = _uiState.value.nameInput.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty.") }
            return
        }
        _uiState.update { it.copy(isSavingName = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = profileRepository.updateDisplayName(name)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSavingName = false, isEditingName = false, profile = result.data)
                }
                is ApiResult.Failure -> _uiState.update {
                    it.copy(isSavingName = false, errorMessage = result.message)
                }
            }
        }
    }
}
