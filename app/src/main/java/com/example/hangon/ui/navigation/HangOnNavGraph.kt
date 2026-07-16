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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hangon.ui.screens.FamilyScreen
import com.example.hangon.ui.screens.HomeScreen
import com.example.hangon.ui.screens.PermissionState
import com.example.hangon.ui.screens.SplashScreen
import com.example.hangon.ui.theme.BackgroundLight
import com.example.hangon.ui.theme.HangOnBlue
import com.example.hangon.ui.theme.SurfaceWhite
import com.example.hangon.ui.theme.TextSecondary

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun HangOnNavGraph() {
    val navController = rememberNavController()

    // --- Hoisted state: persists across Home <-> Family navigation ---
    var appActivated by remember { mutableStateOf(true) }
    val permissions = remember {
        mutableStateListOf(
            PermissionState("contacts", "Contact Access", "Mencocokkan nomor masuk dengan kontak Anda.", false, true),
            PermissionState("audio", "Call Audio Access", "Diperlukan untuk merekam audio selama panggilan.", true, true),
            PermissionState("overlay", "Display Over Other Apps", "Diperlukan untuk menampilkan overlay peringatan.", true, false)
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 8 })
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    // Splash → Home directly (no Permission onboarding screen)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            MainScaffold(navController = navController, currentRoute = Screen.Home.route) {
                HomeScreen(
                    appActivated = appActivated,
                    onAppActivatedChange = { appActivated = it },
                    permissions = permissions,
                    onPermissionToggle = { index, value ->
                        permissions[index] = permissions[index].copy(isGranted = value)
                    }
                )
            }
        }

        composable(Screen.Family.route) {
            MainScaffold(navController = navController, currentRoute = Screen.Family.route) {
                FamilyScreen()
            }
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    val navItems = listOf(
        NavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem(Screen.Family.route, "Family", Icons.Filled.Groups, Icons.Outlined.Groups)
    )

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
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
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
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
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
