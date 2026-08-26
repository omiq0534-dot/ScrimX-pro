package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

import com.example.FirebaseHelper
import com.example.ui.components.LiveAnnouncementMarquee

data class WheelPrize(
    val coins: Int,
    val label: String,
    val color: Color,
    val textColor: Int = android.graphics.Color.WHITE
)

@Composable
fun SpinWheelIcon(modifier: Modifier = Modifier) {
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
            color = Color.White,
            radius = radius * 0.16f
        )
        // Pointer needle at top
        val needlePath = Path().apply {
            moveTo(center.x, 2.dp.toPx())
            lineTo(center.x - 3.dp.toPx(), 8.dp.toPx())
            lineTo(center.x + 3.dp.toPx(), 8.dp.toPx())
            close()
        }
        drawPath(needlePath, color = Color.White)
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
    var isWatchingVideo by remember { mutableStateOf(false) }
    var videoProgress by remember { mutableStateOf(0f) }
    var videoRewardClaimed by remember { mutableStateOf(false) }

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

    // Video Playing Simulation Effect (Runs smooth, never stuck)
    LaunchedEffect(isWatchingVideo) {
        if (isWatchingVideo) {
            videoProgress = 0f
            videoRewardClaimed = false
            for (i in 1..100) {
                delay(35) // Total 3.5 seconds
                videoProgress = i / 100f
            }
            val rewardAmount = appConfig.watchVideoCoins
            userViewModel.addAppMoney(rewardAmount)
            videoRewardClaimed = true
            isWatchingVideo = false
            Toast.makeText(context, "🎉 +$rewardAmount Coins Added to Wallet!", Toast.LENGTH_SHORT).show()
        }
    }

    // 1. Watch Video & Live Stream Dialog
    if (showWatchDialog) {
        AlertDialog(
            containerColor = Color(0xFF14161F),
            onDismissRequest = { 
                if (!isWatchingVideo) showWatchDialog = false 
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
                        if (isWatchingVideo) "WATCHING VIDEO AD..." else "WATCH & EARN / LIVE",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
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
                    if (isWatchingVideo) {
                        // Interactive Video Screen Simulator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0C0D12))
                                .border(1.dp, Color(0xFF262938), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayCircleFilled,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    "Playing Sponsor Video...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Reward unlocks in a moment",
                                    color = Color(0xFF8E92A4),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Progress Bar
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { videoProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = Color(0xFFFFD700),
                                trackColor = Color(0xFF262938)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "${(videoProgress * 100).toInt()}% completed",
                                color = Color(0xFF8E92A4),
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    } else {
                        Text(
                            "Watch short sponsor videos to earn Coins for entry fees, or tune into live tournament scrims!",
                            color = Color(0xFFC0C4D6),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        // Option 1: Watch Ad (+15 Coins)
                        Button(
                            onClick = {
                                isWatchingVideo = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WATCH SPONSOR AD (+15 COINS)", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
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
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WATCH TOURNAMENT LIVE (YT)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (!isWatchingVideo) {
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
            containerColor = Color(0xFF14161F),
            onDismissRequest = { showDailyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("DAILY REWARD 🎁", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
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
                        color = Color(0xFFC0C4D6),
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
                                                else -> Color(0xFF1A1D27)
                                            }
                                        )
                                        .border(
                                            1.5.dp,
                                            when (status) {
                                                "claimed" -> Color(0xFF00E676)
                                                "today" -> Color(0xFFFFD700)
                                                else -> Color(0xFF2E3346)
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
                    colors = ButtonDefaults.buttonColors(containerColor = if (dailyClaimed) Color(0xFF2E3346) else Color(0xFFFFD700)),
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
            containerColor = Color(0xFF14161F),
            onDismissRequest = { if (!isSpinning) showSpinDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LUCKY SPIN WHEEL 🎰", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
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
                                            color = Color(0xFF14161F),
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
                                    color = if (d % 2 == 0) Color.White else Color(0xFFFFD700),
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
                            containerColor = if (spinsRemaining > 0) Color.White else Color(0xFF374151),
                            disabledContainerColor = Color(0xFF262A38)
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
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { 
            TopWalletBar(
                userName = profile?.name?.ifBlank { "Player" } ?: "Player",
                appMoney = profile?.appMoney ?: 0,
                realMoney = profile?.realMoney ?: 0,
                onProfileClick = { onNavigateToTab?.invoke("profile_tab") },
                onWalletClick = { onNavigateToTab?.invoke("wallet_tab") }
            ) 
        }
        if (appConfig.isAnnouncementActive && appConfig.announcementNotice.isNotBlank()) {
            item {
                LiveAnnouncementMarquee(notice = appConfig.announcementNotice)
            }
        }
        item { 
            EarningZone(
                onDailyClick = { showDailyDialog = true },
                onSpinClick = { showSpinDialog = true },
                onWatchClick = { showWatchDialog = true }
            ) 
        }
        item { UpcomingMatches(navController) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun TopWalletBar(
    userName: String = "Player",
    appMoney: Int = 0,
    realMoney: Int = 0,
    onProfileClick: () -> Unit = {},
    onWalletClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile & Gamer Tag
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onProfileClick() }
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar with dynamic initial
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF818CF8))
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val initial = userName.trim().firstOrNull()?.toString()?.uppercase() ?: "P"
                Text(initial, color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp)
                // Active status dot
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                        .border(1.dp, Color(0xFF0F172A), CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    "GAMER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8E92A4),
                    letterSpacing = 0.5.sp
                )
                Text(
                    userName,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1
                )
            }
        }

        // Dual Wallet Pill (Click opens Wallet)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF111319))
                .border(1.dp, Color(0xFF262A38), RoundedCornerShape(32.dp))
                .clickable { onWalletClick() }
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Money / Coins
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E212D))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = "Coins", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("$appMoney", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.width(3.dp))
            // Real Cash
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Cash", tint = Color(0xFFFFD700), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("₹$realMoney", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
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
                Icon(Icons.Default.CardGiftcard, contentDescription = "Daily", tint = Color.White, modifier = Modifier.size(28.dp))
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
                title = "Watch",
                modifier = Modifier.weight(1f),
                onClick = onWatchClick
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Watch", tint = Color.White, modifier = Modifier.size(28.dp))
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .background(Color(0xFF111319))
            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(24.dp))
            .padding(vertical = 20.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E212D)),
            contentAlignment = Alignment.Center
        ) {
            iconContent()
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
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
                    .background(Color(0xFF111319))
                    .border(1.5.dp, Color(0xFF262A38), RoundedCornerShape(26.dp))
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No upcoming scrims right now.\nCheck back shortly or create one from Admin!",
                    color = Color.White,
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
