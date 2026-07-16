package com.example.hangon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

private const val CODEWORD_MAX_TRIES = 2
private const val FALLBACK_DEMO_CODEWORD = "123456"
private const val MONITORING_TO_SUSPICIOUS_DELAY_MS = 4000L

enum class CallStage {
    RINGING_CONSENT,
    IN_CALL_MONITORING,
    SUSPICIOUS_FLAGGED,
    CODEWORD_PROMPT,
    CODEWORD_INPUT,
    CODEWORD_VERIFIED,
    ENDED
}

data class CallOverlayUiState(
    val callerNumber: String = "+1 2345 678 90",
    val stage: CallStage = CallStage.RINGING_CONSENT,
    val callElapsedSeconds: Int = 0,
    val confidence: Int = 95,
    val suspiciousKeywords: List<String> = listOf("Bank Transfer", "Urgent Payment"),
    val reasoning: String = "This conversation shows signs of a prize scam, where scammers often ask for a confirmation payment.",
    val codewordInput: String = "",
    val triesLeft: Int = CODEWORD_MAX_TRIES,
    val codewordError: String? = null,
    val isVerifyingCodeword: Boolean = false
)

class CallOverlayViewModel(
    callerNumber: String,
    private val familyRepository: FamilyRepository = RetrofitFamilyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallOverlayUiState(callerNumber = callerNumber))
    val uiState: StateFlow<CallOverlayUiState> = _uiState.asStateFlow()

    private var familySecret: String? = null
    private var callTimerJob: Job? = null
    private var autoDetectJob: Job? = null

    init {
        viewModelScope.launch {
            val families = familyRepository.listMyFamilies()
            val firstFamilyId = (families as? ApiResult.Success)?.data?.firstOrNull()?.id ?: return@launch
            val secretResult = familyRepository.getSecret(firstFamilyId)
            familySecret = (secretResult as? ApiResult.Success)?.data
        }
    }

    fun onAcceptWithHangOn() {
        _uiState.update { it.copy(stage = CallStage.IN_CALL_MONITORING) }
        startCallTimer()
        autoDetectJob = viewModelScope.launch {
            delay(MONITORING_TO_SUSPICIOUS_DELAY_MS)
            _uiState.update { it.copy(stage = CallStage.SUSPICIOUS_FLAGGED) }
        }
    }

    fun onSkip() {
        _uiState.update { it.copy(stage = CallStage.ENDED) }
    }

    fun onContinueAfterSuspicious() {
        _uiState.update { it.copy(stage = CallStage.CODEWORD_PROMPT) }
    }

    fun onEndCallFromSuspicious() {
        endCall()
    }

    fun onRequestCodewordInput() {
        _uiState.update { it.copy(stage = CallStage.CODEWORD_INPUT) }
    }

    fun onCodewordInputChange(value: String) {
        _uiState.update { it.copy(codewordInput = value, codewordError = null) }
    }

    fun onVerifyCodeword() {
        val state = _uiState.value
        if (state.codewordInput.isBlank()) return

        _uiState.update { it.copy(isVerifyingCodeword = true) }

        val expected = familySecret?.let { TotpGenerator.currentCode(it) } ?: FALLBACK_DEMO_CODEWORD
        val isCorrect = state.codewordInput.trim() == expected

        if (isCorrect) {
            _uiState.update { it.copy(isVerifyingCodeword = false, stage = CallStage.CODEWORD_VERIFIED) }
            return
        }

        val remaining = state.triesLeft - 1
        if (remaining <= 0) {
            endCall()
            return
        }

        _uiState.update {
            it.copy(
                isVerifyingCodeword = false,
                triesLeft = remaining,
                codewordInput = "",
                codewordError = "Incorrect code word."
            )
        }
    }

    fun onDeclineDuringCall() {
        endCall()
    }

    private fun endCall() {
        callTimerJob?.cancel()
        autoDetectJob?.cancel()
        _uiState.update { it.copy(stage = CallStage.ENDED) }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(callElapsedSeconds = it.callElapsedSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        callTimerJob?.cancel()
        autoDetectJob?.cancel()
    }
}

class CallOverlayViewModelFactory(private val callerNumber: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CallOverlayViewModel(callerNumber) as T
    }
}
