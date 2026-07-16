package com.example.hangon.data.repository

import androidx.compose.ui.graphics.Color
import com.example.hangon.data.model.FamilyGroup
import com.example.hangon.data.model.FamilyMember
import com.example.hangon.ui.theme.HangOnBlue
import com.example.hangon.ui.theme.SuccessGreen

interface FamilyRepository {
    fun getDummyFamily(): FamilyGroup
    fun createFamily(name: String): FamilyGroup
    fun joinFamily(inviteCode: String): FamilyGroup
    fun nextCodeword(): String
}

class InMemoryFamilyRepository : FamilyRepository {

    private val codewordPool = listOf(
        "TIGER-4421", "SUNSET-8812", "RIVER-6637",
        "CLOUD-2295", "MANGO-7734", "EAGLE-5549"
    )

    override fun getDummyFamily(): FamilyGroup = FamilyGroup(
        id = "fam_001",
        name = "Keluarga Santoso",
        members = listOf(
            FamilyMember("1", "Budi Santoso", "BS", "+62 812-3456-7890", HangOnBlue),
            FamilyMember("2", "Siti Santoso", "SS", "+62 821-9876-5432", Color(0xFF7C3AED)),
            FamilyMember("3", "Andi Santoso", "AS", "+62 856-1234-5678", SuccessGreen)
        ),
        codeword = "MANGO-7734",
        secondsUntilRefresh = 42
    )

    override fun createFamily(name: String): FamilyGroup = getDummyFamily().copy(name = name)

    override fun joinFamily(inviteCode: String): FamilyGroup = getDummyFamily()

    override fun nextCodeword(): String = codewordPool.random()
}
