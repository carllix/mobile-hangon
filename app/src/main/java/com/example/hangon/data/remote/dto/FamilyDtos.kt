package com.example.hangon.data.remote.dto

import com.example.hangon.data.model.FamilyDetail
import com.example.hangon.data.model.FamilyMember
import com.example.hangon.data.model.FamilySummary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamilyCreateRequestDto(val name: String)

@Serializable
data class FamilyJoinRequestDto(@SerialName("invite_code") val inviteCode: String)

@Serializable
data class FamilyWithSecretResponseDto(
    val id: String,
    val name: String,
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("shared_secret") val sharedSecret: String
)

@Serializable
data class FamilyMemberResponseDto(
    @SerialName("user_id") val userId: String,
    val role: String,
    @SerialName("display_name") val displayName: String?
)

@Serializable
data class FamilyMembersResponseDto(val members: List<FamilyMemberResponseDto>)

@Serializable
data class FamilySecretResponseDto(@SerialName("shared_secret") val sharedSecret: String)

@Serializable
data class FamilySummaryResponseDto(
    val id: String,
    val name: String,
    @SerialName("invite_code") val inviteCode: String,
    val role: String,
    @SerialName("member_count") val memberCount: Int,
    @SerialName("member_preview_names") val memberPreviewNames: List<String>
)

@Serializable
data class MyFamiliesResponseDto(val families: List<FamilySummaryResponseDto>)

fun FamilySummaryResponseDto.toDomain() = FamilySummary(
    id = id,
    name = name,
    inviteCode = inviteCode,
    role = role,
    memberCount = memberCount,
    memberPreviewNames = memberPreviewNames
)

fun FamilyMemberResponseDto.toDomain() = FamilyMember(
    userId = userId,
    displayName = displayName ?: "Member",
    role = role
)

fun FamilyMembersResponseDto.toDomain(
    familyId: String,
    familyName: String,
    inviteCode: String,
    myRole: String
) = FamilyDetail(
    id = familyId,
    name = familyName,
    inviteCode = inviteCode,
    myRole = myRole,
    members = members.map { it.toDomain() }
)
