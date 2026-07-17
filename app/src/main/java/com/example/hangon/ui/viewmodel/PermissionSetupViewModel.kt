package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hangon.data.model.Permission
import com.example.hangon.data.repository.AndroidPermissionRepository
import com.example.hangon.data.repository.PermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PermissionSetupUiState(
    val permissions: List<Permission> = emptyList()
) {
    val allRequiredGranted: Boolean
        get() = permissions.filter { it.isRequired }.all { it.isGranted }
}

class PermissionSetupViewModel(
    private val permissionRepository: PermissionRepository = AndroidPermissionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PermissionSetupUiState(permissions = permissionRepository.getOnboardingPermissions())
    )
    val uiState: StateFlow<PermissionSetupUiState> = _uiState.asStateFlow()

    fun refresh() {
        _uiState.update { it.copy(permissions = permissionRepository.getOnboardingPermissions()) }
    }
}
