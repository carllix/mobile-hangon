package com.example.hangon.data.remote

import com.example.hangon.data.remote.dto.MeResponseDto
import com.example.hangon.data.remote.dto.UpdateDisplayNameRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH

interface UserApi {
    @GET("me")
    suspend fun getMe(@Header("Authorization") auth: String): MeResponseDto

    @PATCH("me")
    suspend fun updateMe(
        @Header("Authorization") auth: String,
        @Body body: UpdateDisplayNameRequestDto
    ): MeResponseDto
}
