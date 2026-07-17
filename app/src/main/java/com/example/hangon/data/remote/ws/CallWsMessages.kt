package com.example.hangon.data.remote.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class StartSessionMessage(
    @SerialName("phone_number") val phoneNumber: String,
    val type: String = "start_session"
)

@Serializable
data class UserDecisionMessage(
    val choice: String,
    val type: String = "user_decision"
)

@Serializable
data class VoiceCheckResponseMessage(
    val recognized: Boolean,
    @SerialName("family_id") val familyId: String? = null,
    val type: String = "voice_check_response"
)

@Serializable
data class CodewordSubmitMessage(
    val value: String,
    val type: String = "codeword_submit"
)

@Serializable
data class EndSessionMessage(val type: String = "end_session")

object UserDecisionChoice {
    const val TUTUP = "tutup"
    const val LANJUTKAN = "lanjutkan"
    const val LANJUT_RISIKO_SENDIRI = "lanjut_risiko_sendiri"
}

sealed class CallServerEvent {
    data class SessionStarted(val callId: String) : CallServerEvent()
    data class AnalysisResult(val isSuspicious: Boolean, val confidence: Double, val reason: String) : CallServerEvent()
    data object RequestVoiceCheck : CallServerEvent()
    data object HighRiskWarning : CallServerEvent()
    data object RequestCodeword : CallServerEvent()
    data class CodewordResult(val verified: Boolean, val retriesLeft: Int?) : CallServerEvent()
    data object ResumedMonitoring : CallServerEvent()
    data class SessionSummary(val logId: String, val verificationStatus: String) : CallServerEvent()
    data class Error(val detail: String) : CallServerEvent()
}

private val json = Json { ignoreUnknownKeys = true }

fun parseCallServerEvent(text: String): CallServerEvent? {
    val obj = json.parseToJsonElement(text).jsonObject
    return when (obj["type"]?.jsonPrimitive?.contentOrNull) {
        "session_started" -> CallServerEvent.SessionStarted(
            callId = obj["call_id"]?.jsonPrimitive?.contentOrNull.orEmpty()
        )
        "analysis_result" -> CallServerEvent.AnalysisResult(
            isSuspicious = obj["is_suspicious"]?.jsonPrimitive?.boolean ?: false,
            confidence = obj["confidence"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            reason = obj["reason"]?.jsonPrimitive?.contentOrNull.orEmpty()
        )
        "request_voice_check" -> CallServerEvent.RequestVoiceCheck
        "high_risk_warning" -> CallServerEvent.HighRiskWarning
        "request_codeword" -> CallServerEvent.RequestCodeword
        "codeword_result" -> CallServerEvent.CodewordResult(
            verified = obj["status"]?.jsonPrimitive?.contentOrNull == "verified",
            retriesLeft = obj["retries_left"]?.jsonPrimitive?.intOrNull
        )
        "resumed_monitoring" -> CallServerEvent.ResumedMonitoring
        "session_summary" -> CallServerEvent.SessionSummary(
            logId = obj["log_id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            verificationStatus = obj["verification_status"]?.jsonPrimitive?.contentOrNull ?: "tidak_tersedia"
        )
        "error" -> CallServerEvent.Error(obj["detail"]?.jsonPrimitive?.contentOrNull ?: "Unknown error")
        else -> null
    }
}
