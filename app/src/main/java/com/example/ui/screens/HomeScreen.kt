package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ads.UnityAdsManager
import com.example.ui.theme.AppColors
import com.example.ui.theme.GlassColors
import com.example.ui.theme.WhiteGlassCard
import com.example.ui.theme.glassCard
import com.example.FirebaseHelper
import com.example.ui.components.XBadge
import com.example.ui.components.XBadgeSize
import com.example.ui.components.AdminMasterBadge
import com.example.ui.components.LiveAnnouncementMarquee
import com.example.ui.components.LivePatchBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class WheelPrize(
    val coins: Int,
    val label: String,
    val color: Color,
    val textColor: Int = android.graphics.Color.WHITE
)

@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToTab: ((String) -> Unit)? = null,
    userViewModel: UserViewModel = viewModel(),
    appControlViewModel: AppControlViewModel = viewModel()
) {
    val profile by userViewModel.profile.collectAsState()
    val appConfig by appControlViewModel.config.collectAsState()
    val context = LocalContext.current
    
    // --- Logic States (Preserved) ---
    var showWatchDialog by remember { mutableStateOf(false) }
    var isAdLoading by remember { mutableStateOf(false) }
    var showDailyDialog by remember { mutableStateOf(false) }
    var showSpinDialog by remember { mutableStateOf(false) }

    val dailyPrefs = remember { context.getSharedPreferences("daily_reward_prefs", android.content.Context.MODE_PRIVATE) }
    val todayDate = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    val lastDailyDate = dailyPrefs.getString("last_claim_date", "") ?: ""
    val savedStreak = dailyPrefs.getInt("streak_count", 0)
    
    var dailyClaimed by remember(todayDate, lastDailyDate) { mutableStateOf(lastDailyDate == todayDate) }
    var currentStreak by remember(todayDate, lastDailyDate, savedStreak) {
        val yesterdayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L))
        val validStreak = when {
            lastDailyDate == todayDate -> savedStreak
            lastDailyDate == yesterdayDate -> savedStreak
            else -> 0
        }
        mutableIntStateOf(validStreak)
    }

    val spinPrefs = remember { context.getSharedPreferences("spin_preferences", android.content.Context.MODE_PRIVATE) }
    val maxDailySpins = appConfig.dailyFreeSpins
    var spinsRemaining by remember(maxDailySpins) {
        val lastDate = spinPrefs.getString("last_spin_date", "") ?: ""
        if (lastDate != todayDate) {
            spinPrefs.edit().putString("last_spin_date", todayDate).putInt("spins_count", 0).apply()
            mutableIntStateOf(maxDailySpins)
        } else {
            val used = spinPrefs.getInt("spins_count", 0)
            mutableIntStateOf((maxDailySpins - used).coerceAtLeast(0))
        }
    }

    // Main UI Layout using Glassmorphism
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        
        // 1. Sleek Glass Top Bar (Profile & Coin Balance)
        item {
            WhiteGlassCard(cornerRadius = 32.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Section
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
                            Text((profile?.name ?: "User").take(1).uppercase(), fontWeight = FontWeight.Bold, color = GlassColors.TextPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text((profile?.name ?: "User"), fontWeight = FontWeight.Black, fontSize = 15.sp, color = GlassColors.TextPrimary)
                                if ((profile?.hasXBadge == true)) { Spacer(modifier = Modifier.width(4.dp)); XBadge() }
                            }
                            Text("ID: ${(profile?.uid ?: "000000").take(6).uppercase()}", fontSize = 11.sp, color = GlassColors.TextSecondary)
                        }
                    }
                    
                    // Coin Counter Pill
                    Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.5f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = GlassColors.GoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text((profile?.appMoney ?: 0).toString(), fontWeight = FontWeight.Black, fontSize = 14.sp, color = GlassColors.TextPrimary)
                        }
                    }
                }
            }
        }
        
        // 2. Canva / Graphic Preview Banner
        item {
            WhiteGlassCard(cornerRadius = 24.dp) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(24.dp)).background(Brush.horizontalGradient(listOf(Color(0xFFD8B4FE), Color(0xFF818CF8))))) {
                    // Placeholder for a high-quality graphic banner
                    Column(modifier = Modifier.padding(20.dp).align(Alignment.CenterStart)) {
                        Text("PRO SCRIMS", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.White)
                        Text("Join daily matches & win massive rewards.", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }
        }
        
        // 3. Main Action Cards Grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WhiteGlassCard(modifier = Modifier.weight(1f).height(120.dp).clickable { onNavigateToTab?.invoke("matches_tab") }) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.Start) {
                        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(GlassColors.Info.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = GlassColors.Info, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Matches", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GlassColors.TextPrimary)
                        Text("Play to Win", fontSize = 11.sp, color = GlassColors.TextSecondary)
                    }
                }
                WhiteGlassCard(modifier = Modifier.weight(1f).height(120.dp).clickable { navController.navigate("store") }) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.Start) {
                        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(GlassColors.GoldAccent.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = GlassColors.GoldAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Store", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GlassColors.TextPrimary)
                        Text("Redeem Rewards", fontSize = 11.sp, color = GlassColors.TextSecondary)
                    }
                }
            }
        }
        
        // 4. Rewards Section Title
        item {
            Text("Daily Tasks & Rewards", fontWeight = FontWeight.Black, fontSize = 18.sp, color = GlassColors.TextPrimary, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
        }

        // Mini Frosted Glass Cards for Rewards
        item {
            // Task: Watch Video
            WhiteGlassCard(modifier = Modifier.fillMaxWidth().clickable { showWatchDialog = true }, cornerRadius = 16.dp) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(GlassColors.Positive.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GlassColors.Positive, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Watch Videos", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GlassColors.TextPrimary)
                        Text("Earn instant coins for entries", fontSize = 12.sp, color = GlassColors.TextSecondary)
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GlassColors.GoldAccent).padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("+${appConfig.watchVideoCoins}", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
        
        item {
            // Task: Daily Check-in
            WhiteGlassCard(modifier = Modifier.fillMaxWidth().clickable { showDailyDialog = true }, cornerRadius = 16.dp) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF3B82F6).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Claim", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GlassColors.TextPrimary)
                        Text("Streak: ${currentStreak} days", fontSize = 12.sp, color = GlassColors.TextSecondary)
                    }
                    if (dailyClaimed) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Claimed", tint = GlassColors.Positive)
                    } else {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GlassColors.TextSecondary)
                    }
                }
            }
        }
        
        item {
            // Task: Spin the Wheel
            WhiteGlassCard(modifier = Modifier.fillMaxWidth().clickable { showSpinDialog = true }, cornerRadius = 16.dp) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(Color(0xFF8B5CF6).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Casino, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lucky Wheel", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GlassColors.TextPrimary)
                        Text("${spinsRemaining} spins left today", fontSize = 12.sp, color = GlassColors.TextSecondary)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GlassColors.TextSecondary)
                }
            }
        }
    }
}
