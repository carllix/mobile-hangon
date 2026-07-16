package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hangon.data.model.FamilyDetail
import com.example.hangon.data.repository.ApiResult
import com.example.hangon.data.repository.FamilyRepository
import com.example.hangon.data.repository.RetrofitFamilyRepository
import com.example.hangon.data.util.TotpGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FamilyDetailUiState(
    val isLoading: Boolean = false,
    val family: FamilyDetail? = null,
    val currentUserId: String? = null,
    val errorMessage: String? = null,
    val currentCodeword: String = "",
    val secondsLeft: Int = 60,
    val isLeaving: Boolean = false,
    val leftFamily: Boolean = false,
    val memberPendingRemoval: String? = null
)

class FamilyDetailViewModel(
    private val familyId: String,
    private val familyRepository: FamilyRepository = RetrofitFamilyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyDetailUiState())
    val uiState: StateFlow<FamilyDetailUiState> = _uiState.asStateFlow()

    private var secret: String? = null
    private var codewordTickerJob: Job? = null

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val detailResult = familyRepository.getFamilyDetail(familyId)
            if (detailResult is ApiResult.Failure) {
                _uiState.update { it.copy(isLoading = false, errorMessage = detailResult.message) }
                return@launch
            }
            val secretResult = familyRepository.getSecret(familyId)
            if (secretResult is ApiResult.Failure) {
                _uiState.update { it.copy(isLoading = false, errorMessage = secretResult.message) }
                return@launch
            }
            val userIdResult = familyRepository.getCurrentUserId()

            val detail = (detailResult as ApiResult.Success).data
            secret = (secretResult as ApiResult.Success).data
            val currentUserId = (userIdResult as? ApiResult.Success)?.data

            _uiState.update {
                it.copy(isLoading = false, family = detail, currentUserId = currentUserId)
            }
            startCodewordTicker()
        }
    }

    fun onLeaveFamily() {
        _uiState.update { it.copy(isLeaving = true) }
        viewModelScope.launch {
            when (val result = familyRepository.leaveFamily(familyId)) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(isLeaving = false, leftFamily = true) }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isLeaving = false, errorMessage = result.message) }
            }
        }
    }

    fun onRequestRemoveMember(userId: String) {
        _uiState.update { it.copy(memberPendingRemoval = userId) }
    }

    fun onCancelRemoveMember() {
        _uiState.update { it.copy(memberPendingRemoval = null) }
    }

    fun onConfirmRemoveMember() {
        val userId = _uiState.value.memberPendingRemoval ?: return
        _uiState.update { it.copy(memberPendingRemoval = null) }
        viewModelScope.launch {
            when (val result = familyRepository.removeMember(familyId, userId)) {
                is ApiResult.Success -> load()
                is ApiResult.Failure -> _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    private fun startCodewordTicker() {
        codewordTickerJob?.cancel()
        codewordTickerJob = viewModelScope.launch {
            while (true) {
                val currentSecret = secret ?: break
                val now = System.currentTimeMillis() / 1000
                _uiState.update {
                    it.copy(
                        currentCodeword = TotpGenerator.currentCode(currentSecret, now),
                        secondsLeft = TotpGenerator.secondsRemainingInWindow(now)
                    )
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        codewordTickerJob?.cancel()
    }
}
