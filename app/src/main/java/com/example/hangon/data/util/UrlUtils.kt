package com.example.hangon.data.util

fun httpToWs(url: String): String {
    val wsScheme = url.replaceFirst(Regex("^http"), "ws")
    return if (wsScheme.endsWith("/")) wsScheme else "$wsScheme/"
}
