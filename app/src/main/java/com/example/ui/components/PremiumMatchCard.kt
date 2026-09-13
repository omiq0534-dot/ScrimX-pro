package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.AppColors
import com.example.ui.theme.glassCard
import com.example.ui.theme.glassBackground
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas

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
    val isFull = slotsBooked >= totalSlots

    // Cleaned up text
    val cleanEntry = entry.replace("₹", "").replace(" Coins", "").trim()
    val cleanPrize = prize.replace("₹", "").replace(" Coins", "").trim()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x28FFFFFF), // Frosted glass top-left highlight
                        Color(0x12FFFFFF), // Translucent dark glass core
                        Color(0x1800E676)  // Bottom-left subtle green ambient reflection
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x4DFFFFFF), // Crisp glass specular top rim
                        Color(0x1AFFFFFF), // Mid frosted edge
                        Color(0xFF00E676).copy(alpha = 0.65f) // Vibrant neon green accent border at bottom-left
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                ),
                width = 1.2.dp,
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        // High-End Glassmorphic Canvas with Bottom-Left Neon Green Cyber Glow
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // 1. Radial Light Green Neon Glow at Bottom-Left Corner (Vibrant & prominent)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E676).copy(alpha = 0.32f), // Bright neon green core
                        Color(0xFF00E676).copy(alpha = 0.16f),
                        Color(0xFF00E676).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(0f, h),
                    radius = (w.coerceAtLeast(h)) * 0.75f
                ),
                center = Offset(0f, h),
                radius = (w.coerceAtLeast(h)) * 0.75f
            )

            // 2. Translucent Frosted Glass Diagonal Sheen / Light Streak
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w * 0.8f, h * 0.6f)
                )
            )

            // 3. Crisp Frosted Top Glass Edge Highlight
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, 1f),
                end = Offset(w * 0.7f, 1f),
                strokeWidth = 2.5f
            )

            // 4. Laser Green Accent Line along bottom-left border
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00E676).copy(alpha = 0.9f),
                        Color(0xFF00E676).copy(alpha = 0.4f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, h - 1f),
                end = Offset(w * 0.5f, h - 1f),
                strokeWidth = 3f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title & Time (Focus on Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = Color(0xFFB0B0B0),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = time,
                            color = Color(0xFFB0B0B0),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Map & Mode Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x2EFFFFFF))
                        .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$map • $badge",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Info Box: Price & Join
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Section
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Prize Info
                    Column {
                        Text(
                            "PRIZE POOL",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                cleanPrize,
                                color = Color.White,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Entry Fee Info
                    Column {
                        Text(
                            "ENTRY FEE",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                cleanEntry,
                                color = Color.White,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Call to Action (Vibrant Button)
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLive || isCompleted) Color(0xFF2E3348) else Color.White,
                        contentColor = if (isLive || isCompleted) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isLive || isCompleted) 0.dp else 4.dp
                    )
                ) {
                    Text(
                        text = when {
                            isLive -> "WATCH"
                            isCompleted -> "RESULTS"
                            isFull -> "FULL"
                            else -> "JOIN"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
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
                    progress = { if (totalSlots > 0) slotsBooked.toFloat() / totalSlots else 0f },
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF00E676),
                    trackColor = Color(0x33FFFFFF),
                )
                Text(
                    "$slotsBooked/$totalSlots",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
