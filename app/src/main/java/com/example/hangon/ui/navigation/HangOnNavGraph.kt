package com.example.hangon.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hangon.ui.screens.FamilyDetailScreen
import com.example.hangon.ui.screens.FamilyListScreen
import com.example.hangon.ui.screens.HomeScreen
import com.example.hangon.ui.screens.LoginScreen
import com.example.hangon.ui.screens.RegisterScreen
import com.example.hangon.ui.theme.BackgroundLight
import com.example.hangon.ui.theme.HangOnBlue
import com.example.hangon.ui.theme.SurfaceWhite
import com.example.hangon.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseAuth

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun HangOnNavGraph(startDestination: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        NavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem(Screen.Family.route, "Family", Icons.Filled.Groups, Icons.Outlined.Groups)
    )

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            if (currentRoute == Screen.Home.route || currentRoute == Screen.Family.route) {
                HangOnBottomBar(
                    items = navItems,
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        if (item.route != currentRoute) {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 8 })
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Family.route) {
                FamilyListScreen(
                    onFamilyClick = { familyId ->
                        navController.navigate(Screen.FamilyDetail.buildRoute(familyId))
                    }
                )
            }

            composable(
                Screen.FamilyDetail.route,
                arguments = listOf(navArgument("familyId") { type = NavType.StringType })
            ) { entry ->
                FamilyDetailScreen(
                    familyId = entry.arguments?.getString("familyId").orEmpty(),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun HangOnBottomBar(
    items: List<NavItem>,
    currentRoute: String,
    onItemSelected: (NavItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceWhite,
        shadowElevation = 16.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                BottomNavTab(
                    item = item,
                    selected = selected,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BottomNavTab(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val iconColor = if (selected) HangOnBlue else TextSecondary
    val textColor = if (selected) HangOnBlue else TextSecondary
    val fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(HangOnBlue)
            )
        } else {
            Box(modifier = Modifier.height(3.dp))
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))

        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = item.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = fontWeight
        )
    }
}
