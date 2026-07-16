package com.example.hangon.data.model

import androidx.compose.ui.graphics.Color

data class FamilyMember(
    val id: String,
    val name: String,
    val initials: String,
    val phone: String,
    val avatarColor: Color
)

data class FamilyGroup(
    val id: String,
    val name: String,
    val members: List<FamilyMember>,
    val codeword: String,
    val secondsUntilRefresh: Int
)
