package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hangon.data.local.AppPrefs
import com.example.hangon.data.model.Permission
import com.example.hangon.data.repository.AndroidPermissionRepository
import com.example.hangon.data.repository.PermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val appActivated: Boolean = false,
    val permissions: List<Permission> = emptyList()
)

class HomeViewModel(
    private val permissionRepository: PermissionRepository = AndroidPermissionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            appActivated = computeAppActivated(),
            permissions = permissionRepository.getHomePermissions()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun refresh() {
        _uiState.update {
            it.copy(
                appActivated = computeAppActivated(),
                permissions = permissionRepository.getHomePermissions()
            )
        }
    }

    fun isCallScreeningGranted(): Boolean = permissionRepository.isCallScreeningGranted()

    fun setLocalActivation(activated: Boolean) {
        AppPrefs.isAppActivated = activated
        _uiState.update { it.copy(appActivated = computeAppActivated()) }
    }

    private fun computeAppActivated(): Boolean =
        permissionRepository.isCallScreeningGranted() && AppPrefs.isAppActivated
}
