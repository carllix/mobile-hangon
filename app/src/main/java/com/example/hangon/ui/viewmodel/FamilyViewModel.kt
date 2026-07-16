package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangon.data.model.FamilyGroup
import com.example.hangon.data.repository.FamilyRepository
import com.example.hangon.data.repository.InMemoryFamilyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FamilyUiState(
    val hasFamilyGroup: Boolean = false,
    val family: FamilyGroup? = null,
    val secondsLeft: Int = 60,
    val currentCodeword: String = "",
    val showJoinDialog: Boolean = false,
    val showCreateDialog: Boolean = false
)

class FamilyViewModel(
    private val familyRepository: FamilyRepository = InMemoryFamilyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyUiState())
    val uiState: StateFlow<FamilyUiState> = _uiState.asStateFlow()

    // Kept alive in viewModelScope (not tied to a composable), so the codeword keeps
    // rotating even while the user is on another tab, unlike the old LaunchedEffect
    // that stopped as soon as FamilyGroupView left composition.
    private var codewordTickerJob: Job? = null

    fun onShowJoinDialog(show: Boolean) {
        _uiState.update { it.copy(showJoinDialog = show) }
    }

    fun onShowCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDialog = show) }
    }

    fun onCreateFamily(name: String) {
        val family = familyRepository.createFamily(name)
        activateFamily(family)
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun onJoinFamily(inviteCode: String) {
        val family = familyRepository.joinFamily(inviteCode)
        activateFamily(family)
        _uiState.update { it.copy(showJoinDialog = false) }
    }

    fun onLeaveFamily() {
        codewordTickerJob?.cancel()
        _uiState.update { FamilyUiState() }
    }

    private fun activateFamily(family: FamilyGroup) {
        _uiState.update {
            it.copy(
                hasFamilyGroup = true,
                family = family,
                secondsLeft = family.secondsUntilRefresh,
                currentCodeword = family.codeword
            )
        }
        startCodewordTicker()
    }

    private fun startCodewordTicker() {
        codewordTickerJob?.cancel()
        codewordTickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val secondsLeft = (_uiState.value.secondsLeft - 1).coerceAtLeast(0)
                if (secondsLeft == 0) {
                    _uiState.update {
                        it.copy(secondsLeft = 60, currentCodeword = familyRepository.nextCodeword())
                    }
                } else {
                    _uiState.update { it.copy(secondsLeft = secondsLeft) }
                }
            }
        }
    }
}
