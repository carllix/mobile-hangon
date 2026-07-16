package com.example.hangon.data.remote

import com.example.hangon.data.remote.dto.FamilyCreateRequestDto
import com.example.hangon.data.remote.dto.FamilyJoinRequestDto
import com.example.hangon.data.remote.dto.FamilyMembersResponseDto
import com.example.hangon.data.remote.dto.FamilySecretResponseDto
import com.example.hangon.data.remote.dto.FamilyWithSecretResponseDto
import com.example.hangon.data.remote.dto.MyFamiliesResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FamilyApi {

    @GET("families")
    suspend fun listMyFamilies(@Header("Authorization") auth: String): MyFamiliesResponseDto

    @POST("families")
    suspend fun createFamily(
        @Header("Authorization") auth: String,
        @Body body: FamilyCreateRequestDto
    ): FamilyWithSecretResponseDto

    @POST("families/join")
    suspend fun joinFamily(
        @Header("Authorization") auth: String,
        @Body body: FamilyJoinRequestDto
    ): FamilyWithSecretResponseDto

    @GET("families/{familyId}/members")
    suspend fun getMembers(
        @Header("Authorization") auth: String,
        @Path("familyId") familyId: String
    ): FamilyMembersResponseDto

    @GET("families/{familyId}/secret")
    suspend fun getSecret(
        @Header("Authorization") auth: String,
        @Path("familyId") familyId: String
    ): FamilySecretResponseDto

    @POST("families/{familyId}/rotate-secret")
    suspend fun rotateSecret(
        @Header("Authorization") auth: String,
        @Path("familyId") familyId: String
    ): FamilySecretResponseDto

    @DELETE("families/{familyId}/leave")
    suspend fun leaveFamily(
        @Header("Authorization") auth: String,
        @Path("familyId") familyId: String
    )

    @DELETE("families/{familyId}/members/{userId}")
    suspend fun removeMember(
        @Header("Authorization") auth: String,
        @Path("familyId") familyId: String,
        @Path("userId") userId: String
    )
}
