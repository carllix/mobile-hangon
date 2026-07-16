package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hangon.data.model.Permission
import com.example.hangon.data.repository.InMemoryPermissionRepository
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
    private val permissionRepository: PermissionRepository = InMemoryPermissionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PermissionSetupUiState(permissions = permissionRepository.getOnboardingPermissions())
    )
    val uiState: StateFlow<PermissionSetupUiState> = _uiState.asStateFlow()

    fun onPermissionToggle(index: Int) {
        _uiState.update { state ->
            state.copy(
                permissions = state.permissions.toMutableList().also {
                    it[index] = it[index].copy(isGranted = !it[index].isGranted)
                }
            )
        }
    }
}
