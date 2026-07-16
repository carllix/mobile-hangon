package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.hangon.data.model.Permission
import com.example.hangon.data.repository.InMemoryPermissionRepository
import com.example.hangon.data.repository.PermissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val appActivated: Boolean = true,
    val permissions: List<Permission> = emptyList(),
    val showCallSimulation: Boolean = false
)

class HomeViewModel(
    private val permissionRepository: PermissionRepository = InMemoryPermissionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(permissions = permissionRepository.getHomePermissions())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onAppActivatedChange(activated: Boolean) {
        _uiState.update { it.copy(appActivated = activated) }
    }

    fun onPermissionToggle(index: Int, granted: Boolean) {
        _uiState.update { state ->
            state.copy(
                permissions = state.permissions.toMutableList().also {
                    it[index] = it[index].copy(isGranted = granted)
                }
            )
        }
    }

    fun onShowCallSimulation(show: Boolean) {
        _uiState.update { it.copy(showCallSimulation = show) }
    }
}
