package com.example.hangon.data.remote

import com.example.hangon.data.remote.dto.MeResponseDto
import retrofit2.http.GET
import retrofit2.http.Header

interface UserApi {
    @GET("me")
    suspend fun getMe(@Header("Authorization") auth: String): MeResponseDto
}
