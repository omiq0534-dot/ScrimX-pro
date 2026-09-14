package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.TournamentTimeHelper
import com.example.utils.rememberJoinCountdown
import com.example.utils.rememberResultCountdown
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PremiumMatchCard(
    title: String,
    time: String,
    prize: String,
    entry: String,
    badge: String,
    status: String = "Upcoming",
    map: String = "Erangel",
    slotsBooked: Int = 0,
    totalSlots: Int = 100,
    liveUrl: String = "",
    joinTime: String = "",
    resultTime: String = "",
    isResultDeclared: Boolean = false,
    onClick: () -> Unit
) {
    val isLive = status.equals("Live", ignoreCase = true) || status.equals("Ongoing", ignoreCase = true)
    val isCompleted = status.equals("Completed", ignoreCase = true)
    val isFull = slotsBooked >= totalSlots && totalSlots > 0

    // Real-time ticking state to drive accurate countdowns and status switches
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val joinTimeMillis = remember(joinTime) { TournamentTimeHelper.parseTimeToMillis(joinTime) }
    val resultTimeMillis = remember(resultTime) { TournamentTimeHelper.parseTimeToMillis(resultTime) }

    // Check if results are ready or declared (Updates dynamically when nowMillis passes resultTimeMillis)
    val isResultReady = isResultDeclared || isCompleted || (resultTimeMillis != null && nowMillis >= resultTimeMillis)
    // Check if joining is still locked/pending (Updates dynamically when nowMillis passes joinTimeMillis)
    val isJoinPending = !isResultReady && !isLive && !isCompleted && (joinTimeMillis != null && nowMillis < joinTimeMillis)
    val joinCountdown = rememberJoinCountdown(joinTime)
    val resultCountdown = rememberResultCountdown(resultTime, isResultDeclared)

    val cleanEntry = entry.replace("₹", "").replace(" Coins", "").trim()
    val cleanPrize = prize.replace("₹", "").replace(" Coins", "").trim()
    val prizeValue = cleanPrize.toIntOrNull() ?: 0

    // Check if Mega / Special High-Prize Tournament (Explicit tag or title only)
    val isMegaTournament = badge.equals("Mega", ignoreCase = true) ||
            badge.equals("Special", ignoreCase = true) ||
            title.contains("Mega", ignoreCase = true)

    // Card 3D Physical Press & Push-Down States (Uniform Top-Down 3D Elevation)
    var isCardPressed by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "cardScale"
    )
    val cardOffsetY by animateDpAsState(
        targetValue = if (isCardPressed) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "cardOffsetY"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isCardPressed) 2.dp else if (isMegaTournament) 14.dp else 10.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "cardElevation"
    )

    // Feature 3: Live Pulsing Aura Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseAndLaser")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Feature 4: Continuous Animated Laser Beam for Mega Tournaments
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laserOffset"
    )

    // Live Real-Time Ticking Countdown for Match Time
    val countdownText = rememberLiveCountdown(time, isLive, isCompleted)

    // Join Button 3D States
    var isBtnPressed by remember { mutableStateOf(false) }
    val btnScale by animateFloatAsState(
        targetValue = if (isBtnPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "btnScale"
    )

    // 3D Floating Top-View Glass Slab Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .offset(y = cardOffsetY)
            .scale(cardScale)
            .shadow(
                elevation = cardElevation,
                shape = RoundedCornerShape(22.dp),
                spotColor = if (isMegaTournament) Color(0xFF00E676).copy(alpha = if (isCardPressed) 0.30f else 0.55f)
                            else Color(0xFF00E676).copy(alpha = if (isCardPressed) 0.15f else 0.35f),
                ambientColor = Color(0xFF000000).copy(alpha = 0.85f)
            )
            .clip(RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isCardPressed = true
                        tryAwaitRelease()
                        isCardPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            // Semi-transparent Smoky Obsidian Glass Body
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isMegaTournament) {
                        listOf(
                            Color(0xF5132219), // Elevated emerald sheen top
                            Color(0xF00D151C), // Obsidian center
                            Color(0xF8081118)  // Deep midnight base
                        )
                    } else {
                        listOf(
                            Color(0xF5161D27), // Specular light top
                            Color(0xF00E131B), // Midnight center
                            Color(0xF80A0E15)  // Deep black base
                        )
                    }
                )
            )
            // 3D Top-View Bevel Border
            .border(
                width = if (isMegaTournament) 1.5.dp else 1.3.dp,
                brush = Brush.verticalGradient(
                    colors = if (isMegaTournament) {
                        listOf(
                            Color(0xE6FFFFFF),
                            Color(0xFF00E676),
                            Color(0x3300E676)
                        )
                    } else {
                        listOf(
                            Color(0x80FFFFFF),
                            Color(0x20FFFFFF),
                            Color(0xFF00E676).copy(alpha = 0.55f)
                        )
                    }
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        // Continuous Laser Edge Sweep for Mega Tournaments
        if (isMegaTournament) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val pathLength = size.width + size.height
                val currentOffset = laserOffset % pathLength

                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF00E676),
                            Color.White,
                            Color(0xFF00E676),
                            Color.Transparent
                        ),
                        startX = currentOffset - 150f,
                        endX = currentOffset + 150f
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 3.5f
                )
            }
        }

        // 3D Ambient Depth & Top Specular Lighting
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // 1. Top Specular Glass Bevel Reflection
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isMegaTournament) 0.85f else 0.65f),
                        Color.White.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, 1.2f),
                end = Offset(w * 0.75f, 1.2f),
                strokeWidth = 2.5f
            )

            // 2. Corner Deep Black Contrast Shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF000000).copy(alpha = 0.95f),
                        Color(0xFF05080C).copy(alpha = 0.85f),
                        Color(0xFF090E14).copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    center = Offset(w * 1.05f, -h * 0.1f),
                    radius = (w.coerceAtLeast(h)) * 1.05f
                ),
                center = Offset(w * 1.05f, -h * 0.1f),
                radius = (w.coerceAtLeast(h)) * 1.05f
            )

            // 3. Bottom Green Glow Accent
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E676).copy(alpha = if (isMegaTournament) 0.32f else 0.22f),
                        Color(0xFF00E676).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(-w * 0.05f, h * 1.05f),
                    radius = (w.coerceAtLeast(h)) * 0.75f
                ),
                center = Offset(-w * 0.05f, h * 1.05f),
                radius = (w.coerceAtLeast(h)) * 0.75f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Live Indicator / Status + Map + Mega Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge with Pulsing Beacon Aura
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isResultReady -> Color(0xFF1B2A1E)
                                    isLive -> Color(0xFF3B0F15)
                                    isCompleted -> Color(0xFF141722)
                                    isJoinPending -> Color(0xFF261D0C)
                                    else -> Color(0xFF0D2517)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    isResultReady -> Color(0xFF00E676)
                                    isLive -> Color(0xFFFF5252)
                                    isCompleted -> Color(0xFF8E92A4)
                                    isJoinPending -> Color(0xFFFFB300)
                                    else -> Color(0xFF00E676)
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isLive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.5.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF5252).copy(alpha = pulseAlpha))
                                )
                            } else if (isResultReady) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(10.dp)
                                )
                            } else if (isJoinPending) {
                                Icon(
                                    Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(10.dp)
                                )
                            } else if (!isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .size(6.5.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676).copy(alpha = pulseAlpha))
                                )
                            }

                            Text(
                                text = when {
                                    isResultReady -> "RESULTS READY"
                                    isLive -> "LIVE NOW"
                                    isCompleted -> "COMPLETED"
                                    isJoinPending -> "JOIN PENDING"
                                    else -> "UPCOMING"
                                },
                                color = when {
                                    isResultReady -> Color(0xFF00E676)
                                    isLive -> Color(0xFFFF5252)
                                    isCompleted -> Color(0xFF8E92A4)
                                    isJoinPending -> Color(0xFFFFB300)
                                    else -> Color(0xFF00E676)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.6.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Map & Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x33FFFFFF), Color(0x11000000))
                                )
                            )
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(Color(0x55FFFFFF), Color(0x15FFFFFF))
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (badge.isNotBlank() && !badge.equals("NONE", ignoreCase = true)) "$map • $badge" else map,
                            color = Color(0xFFE2E8F0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Mega Tournament Shimmer Badge (Only if genuinely a Mega Tournament)
                if (isMegaTournament) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00E676), Color(0xFF00B0FF))
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "⚡ MEGA",
                            color = Color.Black,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Match Title (High-contrast typography)
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                maxLines = 1,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time with Live Countdown Clock & Optional Result Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = if (countdownText != null) Color(0xFF00E676) else Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = time,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )

                    if (countdownText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = countdownText,
                                color = Color(0xFF00E676),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Result Time / Countdown Tag
                if (resultTime.isNotBlank() && !isResultReady) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(11.5.dp)
                        )
                        Text(
                            if (resultCountdown != null) "Res: $resultCountdown" else "Res: $resultTime",
                            color = Color(0xFF00E676),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Info Box: Prize, Entry & 3D Raised Glass Join Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metrics: Prize Pool & Entry Fee
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prize Pool Column
                    Column {
                        Text(
                            "PRIZE POOL",
                            color = Color(0xFF8E92A4),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                if (cleanPrize.startsWith("₹")) cleanPrize else "₹$cleanPrize",
                                color = Color.White,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Entry Fee Column
                    Column {
                        Text(
                            "ENTRY FEE",
                            color = Color(0xFF8E92A4),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                if (cleanEntry.equals("FREE", ignoreCase = true)) "FREE" else if (cleanEntry.startsWith("₹")) cleanEntry else "₹$cleanEntry",
                                color = if (cleanEntry.equals("FREE", ignoreCase = true)) Color(0xFF00E676) else Color.White,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 3D Raised Glass Button with Dynamic Auto-Switching State
                val buttonText = when {
                    isResultReady -> "RESULTS"
                    isLive -> "WATCH"
                    isCompleted -> "RESULTS"
                    isFull -> "FULL"
                    isJoinPending -> if (joinCountdown != null) "OPENS $joinCountdown" else "OPENS ${joinTime.ifBlank { "SOON" }}"
                    else -> "JOIN"
                }

                Box(
                    modifier = Modifier
                        .scale(btnScale)
                        .defaultMinSize(minHeight = 36.dp)
                        .shadow(
                            elevation = if (isBtnPressed) 2.dp else 7.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = if (isResultReady) Color(0xFFFFD700).copy(alpha = if (isBtnPressed) 0.25f else 0.55f)
                                        else Color(0xFF00E676).copy(alpha = if (isBtnPressed) 0.20f else 0.50f),
                            ambientColor = Color(0xFF000000).copy(alpha = 0.85f)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isBtnPressed = true
                                    tryAwaitRelease()
                                    isBtnPressed = false
                                },
                                onTap = { onClick() }
                            )
                        }
                        .background(
                            brush = when {
                                isResultReady -> Brush.verticalGradient(
                                    colors = if (isBtnPressed) listOf(Color(0xFF2E2408), Color(0xFF1A1404))
                                    else listOf(Color(0xFF3D3008), Color(0xFF141003))
                                )
                                isLive -> Brush.verticalGradient(
                                    colors = if (isBtnPressed) listOf(Color(0xFF190709), Color(0xFF280A0F))
                                    else listOf(Color(0xFF3E1017), Color(0xFF1C0609))
                                )
                                isJoinPending -> Brush.verticalGradient(
                                    colors = if (isBtnPressed) listOf(Color(0xFF1F1808), Color(0xFF120E04))
                                    else listOf(Color(0xFF2C220B), Color(0xFF151004))
                                )
                                isCompleted || isFull -> Brush.verticalGradient(
                                    colors = if (isBtnPressed) listOf(Color(0xFF10121B), Color(0xFF171A26))
                                    else listOf(Color(0xFF202434), Color(0xFF10131B))
                                )
                                else -> Brush.verticalGradient(
                                    colors = if (isBtnPressed) listOf(
                                        Color(0xFF041208),
                                        Color(0xFF092213)
                                    ) else listOf(
                                        Color(0xFF0E381F), // Raised 3D glossy obsidian-green top
                                        Color(0xFF06160C)  // Deep black glass bottom
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            brush = when {
                                isResultReady -> Brush.verticalGradient(
                                    listOf(Color(0xFFFFE082), Color(0xFFFFD700), Color(0x33FFD700))
                                )
                                isLive -> Brush.verticalGradient(
                                    listOf(Color(0xFFFF7B7B), Color(0xFFFF5252), Color(0x33FF5252))
                                )
                                isJoinPending -> Brush.verticalGradient(
                                    listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0x33FFB300))
                                )
                                isCompleted || isFull -> Brush.verticalGradient(
                                    listOf(Color(0x88FFFFFF), Color(0x22FFFFFF))
                                )
                                else -> Brush.verticalGradient(
                                    colors = if (isBtnPressed) listOf(
                                        Color(0xFF00E676).copy(alpha = 0.5f),
                                        Color(0x33FFFFFF)
                                    ) else listOf(
                                        Color(0xFF80FFC0),                   // 3D Top bevel light sheen
                                        Color(0xFF00E676),                   // Radiant neon green
                                        Color(0x3300E676)                    // Bottom translucent edge
                                    )
                                )
                            },
                            width = 1.2.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = if (isJoinPending) 12.dp else 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = buttonText,
                            color = when {
                                isResultReady -> Color(0xFFFFD700)
                                isLive -> Color(0xFFFF5252)
                                isJoinPending -> Color(0xFFFFB300)
                                isCompleted || isFull -> Color(0xFF8E92A4)
                                else -> Color(0xFF00E676)
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = if (isJoinPending) 11.5.sp else 12.5.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )

                        Icon(
                            imageVector = when {
                                isResultReady -> Icons.Default.EmojiEvents
                                isLive -> Icons.Default.PlayArrow
                                isJoinPending -> Icons.Default.LockClock
                                isCompleted || isFull -> Icons.Default.Check
                                else -> Icons.AutoMirrored.Filled.ArrowForward
                            },
                            contentDescription = null,
                            tint = when {
                                isResultReady -> Color(0xFFFFD700)
                                isLive -> Color(0xFFFF5252)
                                isJoinPending -> Color(0xFFFFB300)
                                isCompleted || isFull -> Color(0xFF8E92A4)
                                else -> Color(0xFF00E676)
                            },
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar (Neon Green Accent)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SLOTS BOOKED",
                        color = Color(0xFF8E92A4),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "$slotsBooked / $totalSlots",
                        color = if (isFull) Color(0xFFFF5252) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                val progress = if (totalSlots > 0) (slotsBooked.toFloat() / totalSlots.toFloat()).coerceIn(0f, 1f) else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "slotsProgress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF141A24))
                        .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (isFull) {
                                        listOf(Color(0xFFFF5252), Color(0xFFFF1744))
                                    } else {
                                        listOf(
                                            Color(0xFF00E676),
                                            Color(0xFF69F0AE),
                                            Color(0xFF00E676)
                                        )
                                    }
                                )
                            )
                    )
                }
            }
        }
    }
}

/**
 * Live Countdown Helper (Ticks every 1 second)
 */
@Composable
private fun rememberLiveCountdown(timeStr: String, isLive: Boolean, isCompleted: Boolean): String? {
    if (isLive || isCompleted) return null

    var countdown by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(timeStr) {
        val targetMillis = TournamentTimeHelper.parseTimeToMillis(timeStr) ?: return@LaunchedEffect
        while (true) {
            val now = System.currentTimeMillis()
            val diff = targetMillis - now

            if (diff > 0) {
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                val seconds = (diff / 1000) % 60

                countdown = if (hours > 0) {
                    String.format(Locale.ENGLISH, "In %02dh %02dm %02ds", hours, minutes, seconds)
                } else {
                    String.format(Locale.ENGLISH, "In %02dm %02ds", minutes, seconds)
                }
            } else {
                countdown = null
                break
            }
            delay(1000)
        }
    }

    return countdown
}

/**
 * Smart Time Parser
 */
private fun parseTimeToMillis(timeStr: String): Long? {
    return TournamentTimeHelper.parseTimeToMillis(timeStr)
}
