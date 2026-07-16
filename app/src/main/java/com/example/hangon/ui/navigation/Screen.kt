package com.example.hangon.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object PermissionSetup : Screen("permission_setup")
    object Home : Screen("home")
    object Family : Screen("family")
}
