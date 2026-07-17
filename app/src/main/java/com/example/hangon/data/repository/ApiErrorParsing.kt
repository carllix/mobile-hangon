package com.example.hangon.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun parseApiErrorMessage(rawBody: String?, fallback: String): String {
    if (rawBody.isNullOrBlank()) return fallback
    return try {
        when (val detail = Json.parseToJsonElement(rawBody).jsonObject["detail"]) {
            is JsonArray -> detail.firstOrNull()?.jsonObject?.get("msg")?.jsonPrimitive?.content ?: fallback
            is JsonPrimitive -> if (detail.isString) detail.content else fallback
            else -> fallback
        }
    } catch (e: Exception) {
        fallback
    }
}
