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
            .background(Color(0xFF0A0A0A)) // Pure deep black/dark base
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(20.dp))
    ) {
        // Subtle 3D Glassmorphic Bottom Glow (White/Grey)
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Soft white gradient from bottom
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.03f)
                    ),
                    startY = h * 0.5f,
                    endY = h
                )
            )

            // Top rim highlight for 3D depth
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, 0f),
                end = Offset(w, 0f),
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
                        color = Color.White,
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
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = time,
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Map & Mode Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A1A1A))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$map • $badge",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                            color = Color(0xFF666666),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                cleanPrize,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Entry Fee Info
                    Column {
                        Text(
                            "ENTRY FEE",
                            color = Color(0xFF666666),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                cleanEntry,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Call to Action (White Button, Black Text)
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLive || isCompleted) Color(0xFF222222) else Color.White,
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
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color(0xFF222222),
                )
                Text(
                    "$slotsBooked/$totalSlots",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
