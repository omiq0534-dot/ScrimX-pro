package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.app.Activity
import com.example.ads.UnityAdsManager
import com.example.ads.UnityBannerAd
import androidx.compose.animation.core.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.AppColors
import com.example.ui.theme.glassCard
import com.example.ui.theme.glassBackground
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ui.components.XBadge
import com.example.ui.components.XBadgeSize
import com.example.ui.components.AdminMasterBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard
import com.example.ui.components.LiveAnnouncementMarquee
import com.example.ui.components.LivePatchBanner

data class WheelPrize(
    val coins: Int,
    val label: String,
    val color: Color,
    val textColor: Int = android.graphics.Color.WHITE
)

@Composable
fun SpinWheelIcon(modifier: Modifier = Modifier) {
    val canvasColor = AppColors.TextPrimary
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val colors = listOf(
            Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFF10B981),
            Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFFFD700)
        )
        val sweep = 360f / colors.size
        for (i in colors.indices) {
            drawArc(
                color = colors[i],
                startAngle = i * sweep,
                sweepAngle = sweep,
                useCenter = true
            )
        }
        // Outer golden ring
        drawCircle(
            color = Color(0xFFFFD700),
            radius = radius,
            style = Stroke(width = 2.5.dp.toPx())
        )
        // Center hub
        drawCircle(
            color = Color(0xFF0F1118),
            radius = radius * 0.38f
        )
        drawCircle(
            color = canvasColor,
            radius = radius * 0.16f
        )
        // Pointer needle at top
        val needlePath = Path().apply {
            moveTo(center.x, 2.dp.toPx())
            lineTo(center.x - 3.dp.toPx(), 8.dp.toPx())
            lineTo(center.x + 3.dp.toPx(), 8.dp.toPx())
            close()
        }
        drawPath(needlePath, color = canvasColor)
    }
}

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
    val db = remember { FirebaseHelper.getFirestore() }
    val scope = rememberCoroutineScope()

    // Dialog States
    var showWatchDialog by remember { mutableStateOf(false) }
    var isAdLoading by remember { mutableStateOf(false) }

    var showDailyDialog by remember { mutableStateOf(false) }
    val dailyPrefs = remember { context.getSharedPreferences("daily_reward_prefs", android.content.Context.MODE_PRIVATE) }
    val todayDate = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    val lastDailyDate = dailyPrefs.getString("last_claim_date", "") ?: ""
    val savedStreak = dailyPrefs.getInt("streak_count", 0)

    var dailyClaimed by remember(todayDate, lastDailyDate) {
        mutableStateOf(lastDailyDate == todayDate)
    }

    var currentStreak by remember(todayDate, lastDailyDate, savedStreak) {
        val yesterdayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L))
        val validStreak = when {
            lastDailyDate == todayDate -> savedStreak
            lastDailyDate == yesterdayDate -> savedStreak
            else -> 0
        }
        mutableIntStateOf(validStreak)
    }

    var showSpinDialog by remember { mutableStateOf(false) }
    var isSpinning by remember { mutableStateOf(false) }
    var spinReward by remember { mutableStateOf<Int?>(null) }
    val spinRotation = remember { Animatable(0f) }
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

    val jackpotAmount = appConfig.spinJackpot
    val wheelPrizes = remember(jackpotAmount) {
        listOf(
            WheelPrize(10, "+10", Color(0xFFEF4444)),
            WheelPrize(25, "+25", Color(0xFF2563EB)),
            WheelPrize(5, "+5", Color(0xFF10B981)),
            WheelPrize(50, "+50", Color(0xFFF59E0B)),
            WheelPrize(15, "+15", Color(0xFF8B5CF6)),
            WheelPrize(100, "+100", Color(0xFFD946EF)),
            WheelPrize(20, "+20", Color(0xFF06B6D4)),
            WheelPrize(jackpotAmount, "👑 $jackpotAmount", Color(0xFFFFD700), android.graphics.Color.BLACK)
        )
    }

    // 1. Watch Video & Live Stream Dialog
    if (showWatchDialog) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { 
                if (!isAdLoading) showWatchDialog = false 
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "WATCH & EARN / LIVE",
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextPrimary,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Watch short sponsor videos to earn Coins for entry fees, or tune into live tournament scrims!",
                        color = Color(0xFF4B5563),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Option 1: Watch Unity Video Ad (+15 Coins)
                    Button(
                        onClick = {
                            val currentActivity = context as? Activity
                            if (currentActivity != null) {
                                isAdLoading = true
                                Toast.makeText(context, "🎬 Loading Sponsor Video Ad...", Toast.LENGTH_SHORT).show()
                                UnityAdsManager.showRewardedAd(
                                    activity = currentActivity,
                                    onRewardEarned = {
                                        isAdLoading = false
                                        val rewardAmount = appConfig.watchVideoCoins
                                        userViewModel.addAppMoney(rewardAmount)
                                        Toast.makeText(context, "🎉 +$rewardAmount Coins Added to Wallet!", Toast.LENGTH_LONG).show()
                                        showWatchDialog = false
                                    },
                                    onAdClosed = {
                                        isAdLoading = false
                                    },
                                    onAdSkipped = {
                                        isAdLoading = false
                                        Toast.makeText(context, "⚠️ Video was skipped! Reward claim karne ke liye poora ad dekhna zaroori hai.", Toast.LENGTH_SHORT).show()
                                    },
                                    onAdFailed = { err ->
                                        isAdLoading = false
                                        Toast.makeText(context, "⚠️ Ad loading: $err. Please tap again.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isAdLoading
                    ) {
                        if (isAdLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WATCH SPONSOR AD (+${appConfig.watchVideoCoins} COINS)", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }

                    // Option 2: Watch Live Scrims (YouTube)
                        OutlinedButton(
                            onClick = {
                                showWatchDialog = false
                                if (db != null) {
                                    db.collection("settings").document("live_stream").get().addOnSuccessListener { doc ->
                                        val streamUrl = doc.getString("url")
                                        val targetUrl = if (!streamUrl.isNullOrBlank()) streamUrl else "https://www.youtube.com/results?search_query=Free+Fire+Tournament+Live"
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Opening YouTube in browser...", Toast.LENGTH_SHORT).show()
                                        }
                                    }.addOnFailureListener {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=Free+Fire+Tournament+Live")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open stream", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=Free+Fire+Tournament+Live")).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open stream", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3042)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WATCH TOURNAMENT LIVE (YT)", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                }
            },
            confirmButton = {
                if (!isAdLoading) {
                    TextButton(onClick = { showWatchDialog = false }) {
                        Text("Close", color = Color(0xFF8E92A4), fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // 2. Daily Check-in Dialog
    if (showDailyDialog) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { showDailyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("DAILY REWARD 🎁", fontWeight = FontWeight.Black, color = AppColors.TextPrimary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00E676).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Streak: $currentStreak/7 Days",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        if (dailyClaimed) "Already claimed today! Come back tomorrow for Day ${if (currentStreak >= 7) 1 else currentStreak + 1}." else "Claim your free +${appConfig.dailyRewardCoins} coins today!",
                        color = Color(0xFF4B5563),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    // 7-Day Streak UI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (day in 1..7) {
                            val status = when {
                                day <= currentStreak -> "claimed"
                                day == currentStreak + 1 && !dailyClaimed -> "today"
                                else -> "upcoming"
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "D$day",
                                    color = if (status == "today") Color(0xFFFFD700) else Color(0xFF8E92A4),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (status) {
                                                "claimed" -> Color(0xFF00E676).copy(alpha = 0.2f)
                                                "today" -> Color(0xFFFFD700)
                                                else -> Color(0xFFF9FAFB)
                                            }
                                        )
                                        .border(
                                            1.5.dp,
                                            when (status) {
                                                "claimed" -> Color(0xFF00E676)
                                                "today" -> Color(0xFFFFD700)
                                                else -> AppColors.BorderColor
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (status == "claimed") {
                                        Icon(Icons.Default.Check, contentDescription = "Claimed", tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            "$day",
                                            fontWeight = FontWeight.Black,
                                            color = if (status == "today") Color.Black else Color(0xFF9CA3AF),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!dailyClaimed) {
                            val nextStreak = if (currentStreak >= 7) 1 else currentStreak + 1
                            currentStreak = nextStreak
                            dailyClaimed = true
                            dailyPrefs.edit()
                                .putString("last_claim_date", todayDate)
                                .putInt("streak_count", nextStreak)
                                .apply()

                            val reward = appConfig.dailyRewardCoins
                            userViewModel.addAppMoney(reward)
                            Toast.makeText(context, "🎁 +$reward Daily Coins Claimed! (Day $nextStreak/7)", Toast.LENGTH_SHORT).show()
                        } else {
                            showDailyDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (dailyClaimed) AppColors.BorderColor else Color(0xFFFFD700)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        if (dailyClaimed) "CLAIMED TODAY" else "CLAIM +${appConfig.dailyRewardCoins} COINS",
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDailyDialog = false }) {
                    Text("Close", color = Color(0xFF8E92A4))
                }
            }
        )
    }

    // 3. Spin Wheel Dialog
    if (showSpinDialog) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { if (!isSpinning) showSpinDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LUCKY SPIN WHEEL 🎰", fontWeight = FontWeight.Black, color = AppColors.TextPrimary, fontSize = 16.sp)
                    Surface(
                        color = if (spinsRemaining > 0) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Spins: $spinsRemaining/$maxDailySpins",
                            color = if (spinsRemaining > 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Custom Drawn Spin Wheel with Numbers on Slices & Needle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                    ) {
                        val textPrimaryColor = AppColors.TextPrimary
                        Canvas(
                            modifier = Modifier.size(200.dp)
                        ) {
                            val radius = size.minDimension / 2f
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val sweep = 360f / wheelPrizes.size

                            withTransform({ rotate(spinRotation.value) }) {
                                drawIntoCanvas { canvas ->
                                    val nativeCanvas = canvas.nativeCanvas

                                    // 1. Draw colored arcs
                                    for (i in wheelPrizes.indices) {
                                        drawArc(
                                            color = wheelPrizes[i].color,
                                            startAngle = i * sweep,
                                            sweepAngle = sweep,
                                            useCenter = true
                                        )
                                    }

                                    // 2. Draw slice border lines
                                    for (i in wheelPrizes.indices) {
                                        val angleRad = Math.toRadians((i * sweep).toDouble())
                                        val x = (center.x + radius * cos(angleRad)).toFloat()
                                        val y = (center.y + radius * sin(angleRad)).toFloat()
                                        drawLine(
                                            color = Color.White,
                                            start = center,
                                            end = Offset(x, y),
                                            strokeWidth = 2.5.dp.toPx()
                                        )
                                    }

                                    // 3. Draw text numbers on each slice
                                    val paint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        textSize = 13.sp.toPx()
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                    }

                                    for (i in wheelPrizes.indices) {
                                        val midAngle = (i + 0.5f) * sweep
                                        val textAngleRad = Math.toRadians(midAngle.toDouble())
                                        val textDist = radius * 0.65f
                                        val textX = (center.x + textDist * cos(textAngleRad)).toFloat()
                                        val textY = (center.y + textDist * sin(textAngleRad)).toFloat()

                                        nativeCanvas.save()
                                        nativeCanvas.rotate(midAngle + 90f, textX, textY)
                                        paint.color = wheelPrizes[i].textColor
                                        if (wheelPrizes[i].textColor == android.graphics.Color.WHITE) {
                                            paint.setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
                                        } else {
                                            paint.clearShadowLayer()
                                        }
                                        nativeCanvas.drawText(wheelPrizes[i].label, textX, textY + paint.textSize / 3f, paint)
                                        nativeCanvas.restore()
                                    }
                                }
                            }

                            // 4. Outer golden ring
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = radius,
                                style = Stroke(width = 5.dp.toPx())
                            )
                            // Decorative studs
                            val dots = 16
                            for (d in 0 until dots) {
                                val dotAngleRad = Math.toRadians((d * (360.0 / dots)))
                                val dotX = (center.x + (radius - 2.5.dp.toPx()) * cos(dotAngleRad)).toFloat()
                                val dotY = (center.y + (radius - 2.5.dp.toPx()) * sin(dotAngleRad)).toFloat()
                                drawCircle(
                                    color = if (d % 2 == 0) textPrimaryColor else Color(0xFFFFD700),
                                    radius = 2.2.dp.toPx(),
                                    center = Offset(dotX, dotY)
                                )
                            }
                            // Center dark hub with gold rim
                            drawCircle(
                                color = Color(0xFF0F1118),
                                radius = radius * 0.28f
                            )
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = radius * 0.28f,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = radius * 0.12f
                            )
                        }

                        // Wheel Top Pointer Needle
                        Canvas(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-14).dp)
                                .size(width = 28.dp, height = 32.dp)
                        ) {
                            val path = Path().apply {
                                moveTo(size.width / 2f, size.height) // tip
                                lineTo(0f, 0f)
                                lineTo(size.width, 0f)
                                close()
                            }
                            drawPath(path, color = Color(0xFF0F1118))

                            val innerPath = Path().apply {
                                moveTo(size.width / 2f, size.height - 3.dp.toPx())
                                lineTo(3.dp.toPx(), 3.dp.toPx())
                                lineTo(size.width - 3.dp.toPx(), 3.dp.toPx())
                                close()
                            }
                            drawPath(innerPath, color = Color(0xFFFFD700))
                        }
                    }

                    if (spinReward != null) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                        ) {
                            Text(
                                "🎉 Congratulations! Won +$spinReward Coins!",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else if (spinsRemaining == 0) {
                        Text(
                            "Daily 2 free spins used. Come back tomorrow!",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            "Spin the wheel to win up to 200 coins!",
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (!isSpinning && spinsRemaining > 0) {
                                isSpinning = true
                                spinReward = null
                                scope.launch {
                                    val targetPrizeIndex = (0 until wheelPrizes.size).random()
                                    val sweep = 360f / wheelPrizes.size
                                    val sliceCenter = (targetPrizeIndex + 0.5f) * sweep
                                    val targetAngleMod = ((270f - sliceCenter) % 360f + 360f) % 360f
                                    val currentRot = spinRotation.value
                                    val currentMod = ((currentRot % 360f) + 360f) % 360f
                                    val forwardDelta = ((targetAngleMod - currentMod) % 360f + 360f) % 360f
                                    val totalTargetRotation = currentRot + (360f * 6) + forwardDelta

                                    spinRotation.animateTo(
                                        targetValue = totalTargetRotation,
                                        animationSpec = tween(
                                            durationMillis = 3600,
                                            easing = FastOutSlowInEasing
                                        )
                                    )

                                    val won = wheelPrizes[targetPrizeIndex].coins
                                    spinReward = won
                                    userViewModel.addAppMoney(won)
                                    spinsRemaining = (spinsRemaining - 1).coerceAtLeast(0)
                                    val usedCount = maxDailySpins - spinsRemaining
                                    spinPrefs.edit().putString("last_spin_date", todayDate).putInt("spins_count", usedCount).apply()

                                    isSpinning = false
                                    Toast.makeText(context, "🎉 You won +$won Coins!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (spinsRemaining > 0) AppColors.ButtonContainer else AppColors.BorderColor,
                            disabledContainerColor = AppColors.BorderColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isSpinning && spinsRemaining > 0
                    ) {
                        if (isSpinning) {
                            Text("SPINNING WHEEL...", color = Color.Black, fontWeight = FontWeight.Black)
                        } else if (spinsRemaining == 0) {
                            Text("NO SPINS LEFT TODAY", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                        } else {
                            Text("SPIN NOW ($spinsRemaining LEFT)", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            },
            confirmButton = {
                if (!isSpinning) {
                    TextButton(onClick = { showSpinDialog = false }) {
                        Text("Close", color = Color(0xFF8E92A4))
                    }
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { 
            val isOwner = AppSecurityGuard.isSuperOwner(profile?.email)
            val isModerator = (profile?.isModerator == true || profile?.role.equals("moderator", ignoreCase = true)) && !isOwner
            TopWalletBar(
                userName = profile?.name?.ifBlank { "Player" } ?: "Player",
                appMoney = profile?.appMoney ?: 0,
                realMoney = profile?.realMoney ?: 0,
                hasXBadge = profile?.hasXBadge == true,
                isOwner = isOwner,
                isModerator = isModerator,
                onProfileClick = { onNavigateToTab?.invoke("profile_tab") },
                onWalletClick = { onNavigateToTab?.invoke("wallet_tab") },
                onAdminClick = { navController.navigate("admin_dashboard") }
            ) 
        }
        if (appConfig.isAnnouncementActive && appConfig.announcementNotice.isNotBlank()) {
            item {
                LiveAnnouncementMarquee(notice = appConfig.announcementNotice)
            }
        }
        if (appConfig.isLivePatchActive && appConfig.patchTag.isNotBlank()) {
            item {
                LivePatchBanner(tag = appConfig.patchTag, notes = appConfig.patchNotes)
            }
        }
        item { 
            EarningZone(
                onDailyClick = { showDailyDialog = true },
                onSpinClick = { 
                    if (appConfig.isSpinWheelEnabled) {
                        showSpinDialog = true 
                    } else {
                        Toast.makeText(context, "Spin Wheel is temporarily paused by Admin", Toast.LENGTH_SHORT).show()
                    }
                },
                onWatchClick = { 
                    if (appConfig.isWatchAdsEnabled) {
                        showWatchDialog = true 
                    } else {
                        Toast.makeText(context, "Video rewards are temporarily paused by Admin", Toast.LENGTH_SHORT).show()
                    }
                }
            ) 
        }
        item {
            HomeRewardsStoreBanner(
                onClick = { navController.navigate("store") }
            )
        }
        item { UpcomingMatches(navController) }
        item {
            UnityBannerAd(modifier = Modifier.fillMaxWidth())
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun TopWalletBar(
    userName: String = "Player",
    appMoney: Int = 0,
    realMoney: Int = 0,
    hasXBadge: Boolean = false,
    isOwner: Boolean = false,
    isModerator: Boolean = false,
    onProfileClick: () -> Unit = {},
    onWalletClick: () -> Unit = {},
    onAdminClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile & Gamer Tag
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(20.dp))
                .clickable { onProfileClick() }
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar (Clean, minimal, modern)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2028))
                    .border(1.dp, Color(0xFF33384A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initial = userName.trim().firstOrNull()?.toString()?.uppercase() ?: "P"
                Text(
                    text = initial, 
                    color = AppColors.TextPrimary, 
                    fontWeight = FontWeight.Black, 
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isOwner) "👑 SUPREME OWNER" 
                        else if (isModerator) "🛡️ MODERATOR" 
                        else if (hasXBadge) "⚡ PRO [X] CONTENDER" 
                        else "GAMER",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isOwner) Color(0xFFFFD700) 
                                else if (isModerator) Color(0xFF818CF8) 
                                else if (hasXBadge) Color(0xFFFF2A6D) 
                                else Color(0xFF8E92A4),
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOwner || isModerator) {
                        AdminMasterBadge(isOwner = isOwner, size = XBadgeSize.MINI, showClickInfo = true)
                        Spacer(modifier = Modifier.width(4.dp))
                    } else if (hasXBadge) {
                        XBadge(size = XBadgeSize.MINI, isAnimated = true, showClickInfo = true)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        userName,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Action Pills: Quick Admin Hub (if Owner) + Dual Wallet Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if ((isOwner || isModerator) && onAdminClick != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF0F111A))
                        .border(1.dp, Color(0xFFFF0055).copy(alpha = 0.7f), CircleShape)
                        .clickable { onAdminClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = "HQ", tint = Color(0xFFFF0055), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("HQ", fontWeight = FontWeight.Black, fontSize = 10.sp, color = AppColors.TextPrimary, maxLines = 1, softWrap = false)
                    }
                }
            }

            // Dual Wallet Pill (Click opens Wallet)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, AppColors.BorderColor, RoundedCornerShape(24.dp))
                    .clickable { onWalletClick() }
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Money / Coins
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E212D))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Coins", tint = AppColors.TextPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        "$appMoney",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                // Real Cash
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Cash", tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        "₹$realMoney",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun EarningZone(
    onDailyClick: () -> Unit = {},
    onSpinClick: () -> Unit = {},
    onWatchClick: () -> Unit = {}
) {
    Column {
        Text("Earning Zone", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            EarnCard(
                title = "Daily",
                modifier = Modifier.weight(1f),
                onClick = onDailyClick
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = "Daily", tint = AppColors.TextPrimary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            EarnCard(
                title = "Spin",
                modifier = Modifier.weight(1f),
                onClick = onSpinClick
            ) {
                SpinWheelIcon(modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            EarnCard(
                title = "Video",
                modifier = Modifier.weight(1f),
                onClick = onWatchClick
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Watch", tint = AppColors.TextPrimary, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun EarnCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "ScaleAnim")
    
    val baseGlowColor = AppColors.TextPrimary
    val canvasColor = AppColors.TextPrimary

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .glassCard()
    ) {
        // Bottom Inner Glow and 3D effects (Static and Softer)
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Soft static white glow from bottom up (lighter)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        baseGlowColor.copy(alpha = 0.02f), // soft middle alpha
                        baseGlowColor.copy(alpha = 0.1f)   // soft bottom alpha
                    ),
                    startY = h * 0.4f,
                    endY = h
                )
            )

            // Subtle Rim light at the bottom for 3D depth
            drawLine(
                color = baseGlowColor.copy(alpha = 0.25f),
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 8f
            )

            // Top highlight
            drawLine(
                color = canvasColor.copy(alpha = 0.04f),
                start = Offset(0f, 0f),
                end = Offset(w, 0f),
                strokeWidth = 4f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .glassCard(alpha = 0.5f)
                    .border(1.dp, baseGlowColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = AppColors.TextPrimary, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun UpcomingMatches(navController: NavController, viewModel: MatchesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val matches by viewModel.matches.collectAsState()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Upcoming Scrims", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text("See All", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .border(1.5.dp, AppColors.BorderColor, RoundedCornerShape(26.dp))
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No upcoming scrims right now.\nCheck back shortly or create one from Admin!",
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            matches.forEach { match ->
                val totalSlots = if (match.totalSlots > 0) match.totalSlots else if (match.mode == "Solo") 48 else if (match.mode == "Duo") 24 else 12
                com.example.ui.components.PremiumMatchCard(
                    title = match.title,
                    time = match.time,
                    prize = match.prize,
                    entry = match.entry,
                    badge = match.badge,
                    status = match.status,
                    map = match.map,
                    slotsBooked = match.bookedSlots.size,
                    totalSlots = totalSlots,
                    liveUrl = match.liveUrl,
                    onClick = { navController.navigate("match_details/${android.net.Uri.encode(match.id)}") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HomeRewardsStoreBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF18112E), Color(0xFF0B1020))
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color(0xFFFFD700).copy(alpha = 0.5f), Color(0xFF00E676).copy(alpha = 0.5f))
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFF8F00))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = "Store", tint = Color.Black, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "REWARDS STORE",
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                .border(0.8.dp, Color(0xFF00E676), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("HOT 🔥", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 8.5.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Redeem Google Play Codes & VIP Passes with Coins",
                        color = Color(0xFFB0B5C9),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.TextPrimary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Open Store", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
            }
        }
    }
}

