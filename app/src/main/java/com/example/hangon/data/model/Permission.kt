package com.example.hangon.data.model

data class Permission(
    val id: String,
    val title: String,
    val description: String,
    val isRequired: Boolean,
    val isGranted: Boolean
)
