package com.example.hangon.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeResponseDto(
    val id: String,
    @SerialName("firebase_uid") val firebaseUid: String,
    @SerialName("display_name") val displayName: String?
)
