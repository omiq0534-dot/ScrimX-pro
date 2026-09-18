package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.MatchDetailsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme


import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {
  companion object {
    val widgetNavTarget = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    intent.getStringExtra("navigate_to")?.let { target ->
      widgetNavTarget.value = target
    }
  }

  // Force emulator refresh
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    intent.getStringExtra("navigate_to")?.let { target ->
      widgetNavTarget.value = target
    }
    FirebaseHelper.init(this)
    com.example.utils.TelemetrySyncEngine.startSync()
    com.example.ads.UnityAdsManager.syncFromFirestore(this)
    com.example.utils.NotificationHelper.initNotificationChannels(this)
    com.example.ui.theme.ThemeManager.init(this)
    enableEdgeToEdge()
    
    setContent {
      val isDarkTheme = com.example.ui.theme.ThemeManager.isDarkTheme.collectAsState().value
      MyApplicationTheme(darkTheme = isDarkTheme, dynamicColor = false) {

        com.example.ui.components.RequestNotificationPermissionOnLaunch()
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides androidx.compose.ui.graphics.Color.Black) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
              SplashScreen(navController)
            }
            composable("login") {
              LoginScreen(navController)
            }
            composable("home") {
              MainScreen(navController)
            }
            composable("match_details/{matchId}") { backStackEntry ->
              val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
              MatchDetailsScreen(matchId, navController)
            }
            composable("store") {
              com.example.ui.screens.StoreScreen(navController)
            }
            composable("customer_support") {
              com.example.ui.screens.CustomerSupportScreen(navController)
            }
            composable("admin_dashboard") {
              com.example.ui.screens.AdminScreen(navController)
            }
            composable("admin_live_stream") {
              com.example.ui.screens.AdminLiveStreamScreen(navController)
            }
            composable("admin_create_match") {
              com.example.ui.screens.AdminCreateMatchScreen(navController)
            }
            composable("admin_manage_matches") {
              com.example.ui.screens.AdminManageMatchesScreen(navController)
            }
            composable("admin_manage_wallets") {
              com.example.ui.screens.AdminManageWalletsScreen(navController)
            }
            composable("admin_app_update") {
              com.example.ui.screens.AdminAppUpdateScreen(navController)
            }
            composable("admin_user_security") {
              com.example.ui.screens.AdminUserSecurityScreen(navController)
            }
            composable("admin_support") {
              com.example.ui.screens.AdminSupportScreen(navController)
            }
            composable("admin_broadcast_notifications") {
              com.example.ui.screens.AdminBroadcastNotificationScreen(navController)
            }
            composable("admin_staff_management") {
              com.example.ui.screens.AdminStaffManagementScreen(navController)
            }
            composable("admin_unity_ads") {
              com.example.ui.screens.AdminUnityAdsScreen(navController)
            }
            composable("admin_store_codes") {
              com.example.ui.screens.AdminStoreCodesScreen(navController)
            }
            composable("admin_telemetry_connection") {
              com.example.ui.screens.AdminTelemetryConnectionScreen(navController)
            }
            composable("points_table/{matchId}") { backStackEntry ->
              val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
              com.example.ui.screens.PointsTableScreen(matchId, navController)
            }
            composable("admin_points_table/{matchId}") { backStackEntry ->
              val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
              com.example.ui.screens.AdminPointsTableScreen(matchId, navController)
            }
          }
          }
        }
      }
    }
  }
}

