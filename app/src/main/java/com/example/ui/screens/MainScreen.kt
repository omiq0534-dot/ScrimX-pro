package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(rootNavController: NavController) {
    val bottomNavController = rememberNavController()
    
    Scaffold(
        bottomBar = { AppBottomNav(bottomNavController) },
        containerColor = Color(0xFFFAFAFA)
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_tab",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_tab") { 
                HomeScreen(
                    navController = rootNavController,
                    onNavigateToTab = { tab ->
                        bottomNavController.navigate(tab) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) 
            }
            composable("matches_tab") { MatchesScreen(rootNavController) }
            composable("wallet_tab") { WalletScreen(rootNavController) }
            composable("profile_tab") { ProfileScreen(rootNavController) }
        }
    }
}

@Composable
fun AppBottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_angle"
    )

    val sweepBrush = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            Color.Black.copy(alpha = 0.1f),
            Color.Black.copy(alpha = 0.8f),
            Color.Black.copy(alpha = 0.1f),
            Color.Transparent,
            Color.Transparent
        )
    )

    Box(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .drawBehind {
                rotate(angle) {
                    drawCircle(
                        brush = sweepBrush,
                        radius = size.width,
                        center = center
                    )
                }
            }
            .padding(2.dp) // border thickness
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Home
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == "home_tab" } == true,
                onClick = {
                    navController.navigate("home_tab") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home", fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    indicatorColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            // Matches
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == "matches_tab" } == true,
                onClick = {
                    navController.navigate("matches_tab") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Matches") },
                label = { Text("Matches", fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    indicatorColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            // Wallet
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == "wallet_tab" } == true,
                onClick = {
                    navController.navigate("wallet_tab") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                label = { Text("Wallet", fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    indicatorColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
            // Profile
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == "profile_tab" } == true,
                onClick = {
                    navController.navigate("profile_tab") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("Profile", fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    indicatorColor = Color(0xFFF5F5F5),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
