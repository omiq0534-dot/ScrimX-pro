package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    onClick: () -> Unit
) {
    val isLive = status.equals("Live", ignoreCase = true) || status.equals("Ongoing", ignoreCase = true)
    val isCompleted = status.equals("Completed", ignoreCase = true)
    val isFull = slotsBooked >= totalSlots && totalSlots > 0

    // Card 3D Physical Press & Push-Down States
    var isCardPressed by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.962f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "cardScale"
    )
    val cardOffsetY by animateDpAsState(
        targetValue = if (isCardPressed) 5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "cardOffsetY"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isCardPressed) 3.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "cardElevation"
    )

    // Join Button 3D Physical Push-Down State
    var isBtnPressed by remember { mutableStateOf(false) }
    val btnScale by animateFloatAsState(
        targetValue = if (isBtnPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "btnScale"
    )
    val btnOffsetY by animateDpAsState(
        targetValue = if (isBtnPressed) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "btnOffsetY"
    )

    // Cleaned up text
    val cleanEntry = entry.replace("₹", "").replace(" Coins", "").trim()
    val cleanPrize = prize.replace("₹", "").replace(" Coins", "").trim()

    // Outer 3D Base Container (Simulating physical 3D chassis / bottom lip)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .offset(y = cardOffsetY)
            .scale(cardScale)
            // Multi-Layer 3D Physical Shadow (Green under-glow + deep black drop shadow)
            .shadow(
                elevation = cardElevation,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF00E676).copy(alpha = if (isCardPressed) 0.20f else 0.45f),
                ambientColor = Color(0xFF000000).copy(alpha = 0.85f)
            )
            // 3D Chassis Bottom Base (gives genuine physical thickness when resting)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B232E), // 3D Top bevel edge
                        Color(0xFF0D1217), // 3D Chassis core
                        Color(0xFF05080A)  // 3D Bottom shadow lip
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(bottom = if (isCardPressed) 1.dp else 4.dp) // 3D Mechanical Sink Effect
    ) {
        // Main 3D Glass Face Plate
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                // Semi-transparent Smoky Black Glass
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xF2131A24), // Smoky glass specular top
                            Color(0xE80C1017), // Midnight black glass center
                            Color(0xF508140F)  // Emerald-tinted obsidian base
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                // 3D Multi-Layer Chamfered Glass Border
                .border(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x99FFFFFF),                    // 3D Top-left specular prism highlight
                            Color(0x25FFFFFF),                    // Subtle crystal edge
                            Color(0xFF00E676).copy(alpha = 0.75f) // Radiant neon cyber-green bottom rim
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    width = 1.5.dp,
                    shape = RoundedCornerShape(22.dp)
                )
        ) {
            // High-End 3D Canvas Rendering: Top-Right Black Light + Bottom-Left Green Glow + 3D Chamfer
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // 1. TOP-RIGHT CORNER: Deep Obsidian Dark Light / Black Glass Shadow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF000000).copy(alpha = 0.96f),
                            Color(0xFF04070A).copy(alpha = 0.88f),
                            Color(0xFF090E14).copy(alpha = 0.50f),
                            Color.Transparent
                        ),
                        center = Offset(w * 1.05f, -h * 0.1f),
                        radius = (w.coerceAtLeast(h)) * 1.05f
                    ),
                    center = Offset(w * 1.05f, -h * 0.1f),
                    radius = (w.coerceAtLeast(h)) * 1.05f
                )

                // 2. BOTTOM-LEFT CORNER: Radiant Neon Green Ambient Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E676).copy(alpha = 0.30f),
                            Color(0xFF00E676).copy(alpha = 0.14f),
                            Color(0xFF00E676).copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        center = Offset(-w * 0.05f, h * 1.05f),
                        radius = (w.coerceAtLeast(h)) * 0.75f
                    ),
                    center = Offset(-w * 0.05f, h * 1.05f),
                    radius = (w.coerceAtLeast(h)) * 0.75f
                )

                // 3. 3D Diagonal Frosted Glass Light Sheen
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.11f),
                            Color.White.copy(alpha = 0.025f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w * 0.75f, h * 0.75f)
                    )
                )

                // 4. 3D Top Bevel Specular White Highlight
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.75f),
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(16f, 2f),
                    end = Offset(w * 0.65f, 2f),
                    strokeWidth = 3f
                )

                // 5. 3D Bottom Laser Green Accent Reflection Line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF00E676).copy(alpha = 0.95f),
                            Color(0xFF00E676).copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(16f, h - 2f),
                    end = Offset(w * 0.55f, h - 2f),
                    strokeWidth = 3.2f
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header: Status / Mode Pill + Map Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isLive -> Color(0xFFFF3B30)
                                        isCompleted -> Color(0xFF8E92A4)
                                        else -> Color(0xFF00E676)
                                    }
                                )
                        )
                        Text(
                            text = when {
                                isLive -> "LIVE MATCH"
                                isCompleted -> "COMPLETED"
                                else -> "UPCOMING SCRIM"
                            },
                            color = when {
                                isLive -> Color(0xFFFF5252)
                                isCompleted -> Color(0xFF8E92A4)
                                else -> Color(0xFF00E676)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Map & Badge Pill (3D Frosted Glass Pill)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x660B0F15))
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(Color(0x80FFFFFF), Color(0x20FFFFFF))
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "$map • $badge",
                            color = Color(0xFFE2E8F0),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Match Title (High-contrast crisp white typography with 3D depth)
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    maxLines = 1,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Time with high-clarity clock icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = time,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main Info Box: Prize, Entry & 3D Ubhra Hua Green+Black Glass Join Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metrics: Prize Pool & Entry Fee
                    Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                        // Prize Pool Column
                        Column {
                            Text(
                                "PRIZE POOL",
                                color = Color(0xFF8E92A4),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (cleanPrize.startsWith("₹")) cleanPrize else "₹$cleanPrize",
                                    color = Color.White,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Entry Fee Column
                        Column {
                            Text(
                                "ENTRY FEE",
                                color = Color(0xFF8E92A4),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (cleanEntry.equals("FREE", ignoreCase = true)) "FREE" else if (cleanEntry.startsWith("₹")) cleanEntry else "₹$cleanEntry",
                                    color = if (cleanEntry.equals("FREE", ignoreCase = true)) Color(0xFF00E676) else Color.White,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // 3D Ubhra Hua Raised Glass Button with Mechanical Push-Down
                    Box(
                        modifier = Modifier
                            .offset(y = btnOffsetY)
                            .scale(btnScale)
                            // 3D Button Base Shadow
                            .shadow(
                                elevation = if (isBtnPressed) 2.dp else 9.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = Color(0xFF00E676).copy(alpha = if (isBtnPressed) 0.25f else 0.55f),
                                ambientColor = Color(0xFF000000).copy(alpha = 0.85f)
                            )
                            // 3D Button Chassis Rim
                            .background(
                                color = if (isBtnPressed) Color(0xFF030B06) else Color(0xFF082213),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(bottom = if (isBtnPressed) 0.dp else 2.5.dp) // 3D Button Lip
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(13.dp))
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
                                        isLive -> Brush.verticalGradient(
                                            colors = if (isBtnPressed) listOf(Color(0xFF190709), Color(0xFF280A0F))
                                            else listOf(Color(0xFF3E1017), Color(0xFF1C0609))
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
                                    shape = RoundedCornerShape(13.dp)
                                )
                                .border(
                                    brush = when {
                                        isLive -> Brush.verticalGradient(
                                            listOf(Color(0xFFFF7B7B), Color(0xFFFF5252), Color(0x33FF5252))
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
                                    width = 1.5.dp,
                                    shape = RoundedCornerShape(13.dp)
                                )
                                .padding(horizontal = 22.dp, vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = when {
                                        isLive -> "WATCH"
                                        isCompleted -> "RESULTS"
                                        isFull -> "FULL"
                                        else -> "JOIN"
                                    },
                                    color = when {
                                        isLive -> Color(0xFFFF5252)
                                        isCompleted || isFull -> Color(0xFF8E92A4)
                                        else -> Color(0xFF00E676)
                                    },
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.2.sp
                                )
                                if (!isCompleted && !isFull) {
                                    Icon(
                                        if (isLive) Icons.Default.PlayArrow else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = if (isLive) Color(0xFFFF5252) else Color(0xFF00E676),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Progress Bar (Neon Green Accent matching bottom-left corner)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { if (totalSlots > 0) (slotsBooked.toFloat() / totalSlots).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF00E676),
                        trackColor = Color(0x33FFFFFF),
                    )
                    Text(
                        text = "$slotsBooked / $totalSlots SLOTS",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
