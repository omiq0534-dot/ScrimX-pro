package com.example.ui.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.AppColors
import com.example.ui.theme.glassCard
import com.example.ui.theme.glassBackground
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard
import com.example.ui.components.AdminVipTopBanner
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.FloatingRoomLiveBanner
import com.example.ui.components.RoomCredentialsDialog
import com.example.ui.components.ServerMaintenanceScreen
import com.example.ui.components.UserBannedLockScreen
import com.example.utils.NotificationHelper

@Composable
fun MainScreen(
    rootNavController: NavController,
    userViewModel: UserViewModel = viewModel(),
    appControlViewModel: AppControlViewModel = viewModel(),
    matchesViewModel: MatchesViewModel = viewModel()
) {
    val context = LocalContext.current
    val bottomNavController = rememberNavController()
    val userProfile by userViewModel.profile.collectAsState()
    val appConfig by appControlViewModel.config.collectAsState()
    val allMatches by matchesViewModel.matches.collectAsState()

    val isAdmin = remember(userProfile?.email, userProfile?.isModerator, userProfile?.role) {
        AppSecurityGuard.isAuthorizedAdmin(
            email = userProfile?.email,
            isModDocFlag = userProfile?.isModerator == true,
            role = userProfile?.role
        )
    }
    val installedCode = remember(context) { AppControlViewModel.getInstalledVersionCode(context) }
    val installedName = remember(context) { AppControlViewModel.getInstalledVersionName(context) }
    val isAppOutdated = installedCode < appConfig.latestVersionCode
    var dismissUpdateDialog by remember { mutableStateOf(false) }

    // State for in-app Room Credentials Banner & Dialog
    var selectedLiveRoomMatch by remember { mutableStateOf<MatchData?>(null) }
    var showRoomDetailsDialog by remember { mutableStateOf(false) }
    var dismissedLiveMatchIds by remember { mutableStateOf(setOf<String>()) }

    // Auto-detect Room Credentials for Matches Booked by Current User
    val userIdentifier = userProfile?.email?.ifBlank { userProfile?.uid } ?: userProfile?.uid ?: ""
    val activeJoinedLiveMatch = remember(allMatches, userIdentifier, dismissedLiveMatchIds) {
        if (userIdentifier.isBlank()) null
        else {
            allMatches.firstOrNull { match ->
                val isJoined = match.bookedSlots.values.any { it.equals(userProfile?.email, ignoreCase = true) || it == userProfile?.uid }
                val hasRoom = match.roomId.isNotBlank() && match.roomPass.isNotBlank()
                isJoined && hasRoom && !dismissedLiveMatchIds.contains(match.id)
            }
        }
    }

    // Start Presence Tracker for Live Online Users Tracking
    LaunchedEffect(userProfile?.uid, userProfile?.email) {
        val uid = userProfile?.uid ?: ""
        if (uid.isNotBlank()) {
            com.example.utils.PresenceTracker.startTracking(uid)
        }
    }

    // Handle Quick Navigation from Home Screen Widget
    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        val navTarget = activity?.intent?.getStringExtra("navigate_to")
        if (!navTarget.isNullOrBlank()) {
            activity.intent.removeExtra("navigate_to")
            bottomNavController.navigate(navTarget) {
                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Trigger Android System Notification when Room ID & Pass become Live
    LaunchedEffect(allMatches, userProfile?.uid, userProfile?.email) {
        if (userProfile != null) {
            val userEmail = userProfile?.email ?: ""
            val userUid = userProfile?.uid ?: ""
            allMatches.forEach { match ->
                val isJoined = match.bookedSlots.values.any { it.equals(userEmail, ignoreCase = true) || it == userUid }
                if (isJoined && match.roomId.isNotBlank() && match.roomPass.isNotBlank()) {
                    NotificationHelper.showRoomCredentialsNotification(
                        context = context,
                        matchId = match.id,
                        matchTitle = match.title,
                        roomId = match.roomId,
                        roomPass = match.roomPass,
                        gameMode = match.mode
                    )
                }
            }
        }
    }

    // Ban Verification: check if banned and if temporary ban has not expired
    val isTempBanActive = userProfile?.banType == "temporary" && (userProfile?.banUntil ?: 0L) > System.currentTimeMillis()
    val isPermBanActive = userProfile?.banType == "permanent" || (userProfile?.isBanned == true && userProfile?.banType != "temporary")
    val isUserCurrentlyBanned = (isTempBanActive || isPermBanActive) && !isAdmin

    // If User is Banned -> Block with Ban Screen
    if (isUserCurrentlyBanned) {
        UserBannedLockScreen(
            banType = userProfile?.banType ?: "permanent",
            banReason = userProfile?.banReason ?: "Unauthorized usage or violations.",
            banUntil = userProfile?.banUntil ?: 0L,
            onLogout = {
                userViewModel.logout()
                rootNavController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
        return
    }

    // Auto-Ban Trigger: If admin enabled auto-ban for outdated app bypassers
    LaunchedEffect(isAppOutdated, appConfig.autoBanOutdatedUsers, userProfile?.uid) {
        if (isAppOutdated && appConfig.autoBanOutdatedUsers && !isAdmin && userProfile != null) {
            val uid = userProfile!!.uid
            if (uid.isNotBlank()) {
                val db = FirebaseHelper.getFirestore()
                // Apply a 24-hour temporary ban automatically for using outdated app version
                val tempBanUntil = System.currentTimeMillis() + (24 * 3600 * 1000L)
                db?.collection("users")?.document(uid)?.update(
                    mapOf(
                        "isBanned" to true,
                        "banType" to "temporary",
                        "banReason" to "Banned: Attempted to bypass required update with outdated APK.",
                        "banUntil" to tempBanUntil
                    )
                )
            }
        }
    }

    // If regular user and server is in maintenance mode -> Show Fullscreen Maintenance Screen
    if (appConfig.isMaintenanceMode && !isAdmin) {
        ServerMaintenanceScreen(
            message = appConfig.maintenanceMessage,
            onRefresh = {
                // re-evaluated by snapshot listener
            }
        )
        return
    }

    Scaffold(containerColor = Color(0xFFFAFAFA),
        topBar = {
            // Admin VIP Status Indicator Bar when Maintenance or Updates are Active
            if (isAdmin && (appConfig.isMaintenanceMode || isAppOutdated)) {
                val bannerText = when {
                    appConfig.isMaintenanceMode -> "⚠️ MAINTENANCE MODE ACTIVE (Users Blocked • Admin Bypassed)"
                    isAppOutdated -> "📦 NEW UPDATE PUBLISHED: ${appConfig.latestVersionName} (Admin Bypassed)"
                    else -> "👑 ADMIN VIP MODE ACTIVE"
                }
                AdminVipTopBanner(
                    text = bannerText,
                    onAdminClick = { rootNavController.navigate("admin_dashboard") }
                )
            }
        },
        bottomBar = { AppBottomNav(bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = "home_tab"
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
                        },
                        userViewModel = userViewModel,
                        appControlViewModel = appControlViewModel
                    ) 
                }
                composable("matches_tab") { MatchesScreen(rootNavController) }
                composable("wallet_tab") { WalletScreen(rootNavController) }
                composable("profile_tab") { ProfileScreen(rootNavController, userViewModel) }
            }

            // Floating Top Room Alert Banner
            activeJoinedLiveMatch?.let { liveMatch ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    FloatingRoomLiveBanner(
                        match = liveMatch,
                        onClick = {
                            selectedLiveRoomMatch = liveMatch
                            showRoomDetailsDialog = true
                        },
                        onDismiss = {
                            dismissedLiveMatchIds = dismissedLiveMatchIds + liveMatch.id
                        }
                    )
                }
            }

            // Normal Users Update Popup Dialog
            if (isAppOutdated && !isAdmin && !dismissUpdateDialog) {
                AppUpdateDialog(
                    config = appConfig,
                    installedVersionCode = installedCode,
                    installedVersionName = installedName,
                    onDismiss = { dismissUpdateDialog = true }
                )
            }

            // Room Credentials Details Dialog
            if (showRoomDetailsDialog && selectedLiveRoomMatch != null) {
                RoomCredentialsDialog(
                    match = selectedLiveRoomMatch!!,
                    onDismiss = {
                        showRoomDetailsDialog = false
                    }
                )
            }
        }
    }
}


@Composable
fun AppBottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "glow_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(4000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "glow_angle"
    )

    val sweepBrush = androidx.compose.ui.graphics.Brush.sweepGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f),
            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f)
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
            .background(androidx.compose.ui.graphics.Color.White)
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = androidx.compose.ui.graphics.Color.Black,
            tonalElevation = 0.dp
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
                label = { Text("Home", fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                    selectedTextColor = androidx.compose.ui.graphics.Color.Black,
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
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
                    selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                    selectedTextColor = androidx.compose.ui.graphics.Color.Black,
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
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
                    selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                    selectedTextColor = androidx.compose.ui.graphics.Color.Black,
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
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
                    selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                    selectedTextColor = androidx.compose.ui.graphics.Color.Black,
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
                )
            )
        }
    }
}
