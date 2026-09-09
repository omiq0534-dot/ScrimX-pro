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
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .glassCard() // Deep Obsidian Black
            .border(1.2.dp, AppColors.BorderColor, RoundedCornerShape(20.dp))
    ) {
        // Enhanced 3D Glassmorphic Depth (Pure White/Silver Accents)
        val textPrimaryColor = AppColors.TextPrimary
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // 1. Soft Bottom Light Glow (Amplified 3D Depth)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        textPrimaryColor.copy(alpha = 0.02f),
                        textPrimaryColor.copy(alpha = 0.08f)
                    ),
                    startY = h * 0.35f,
                    endY = h
                )
            )

            // 2. Crisp 3D Top Rim Highlight
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        textPrimaryColor.copy(alpha = 0.25f),
                        textPrimaryColor.copy(alpha = 0.05f)
                    )
                ),
                start = Offset(0f, 0f),
                end = Offset(w, 0f),
                strokeWidth = 3.5f
            )

            // 3. Subtle Bottom Reflection Edge
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        textPrimaryColor.copy(alpha = 0.05f),
                        textPrimaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 2f
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
                        .clip(RoundedCornerShape(6.dp))
                        .glassCard()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$map • $badge",
                        color = AppColors.TextPrimary,
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
                            color = Color(0xFF7A7A85),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AppColors.TextPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                cleanPrize,
                                color = AppColors.TextPrimary,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Entry Fee Info
                    Column {
                        Text(
                            "ENTRY FEE",
                            color = Color(0xFF7A7A85),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = AppColors.TextPrimary, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                cleanEntry,
                                color = AppColors.TextPrimary,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Call to Action (White Button, Black Text)
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLive || isCompleted) AppColors.SubCardBackground else AppColors.ButtonContainer,
                        contentColor = if (isLive || isCompleted) AppColors.TextPrimary else AppColors.ButtonContent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (isLive || isCompleted) 0.dp else 5.dp
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

            // Progress Bar (Black and White)
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
                        .height(4.5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AppColors.PrimaryAccent,
                    trackColor = AppColors.SubCardBackground,
                )
                Text(
                    "$slotsBooked/$totalSlots",
                    color = Color(0xFF9E9EA8),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
