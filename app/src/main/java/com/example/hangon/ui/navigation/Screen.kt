package com.example.hangon.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object PermissionSetup : Screen("permission_setup")
    object Home : Screen("home")
    object Family : Screen("family")
    object FamilyDetail : Screen("family_detail/{familyId}") {
        fun buildRoute(familyId: String) = "family_detail/$familyId"
    }
}
