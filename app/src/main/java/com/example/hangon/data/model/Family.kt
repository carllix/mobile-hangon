package com.example.hangon.data.model

data class FamilySummary(
    val id: String,
    val name: String,
    val inviteCode: String,
    val role: String,
    val memberCount: Int,
    val memberPreviewNames: List<String>
)

data class FamilyMember(
    val userId: String,
    val displayName: String,
    val role: String
)

data class FamilyDetail(
    val id: String,
    val name: String,
    val inviteCode: String,
    val myRole: String,
    val members: List<FamilyMember>
)
