package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    // Floating card container with soft 3D ambient shadow
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x26000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111319))
            .border(1.2.dp, Color(0xFF262A38), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            
            // Header Row: Map & Status Indicator + Mode Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isLive -> Color(0xFFFF003C)
                                    isCompleted -> Color(0xFF00C853)
                                    else -> Color(0xFF111827)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                if (isLive) "● LIVE" else if (isCompleted) "FINISHED" else "UPCOMING",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Map Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E212D))
                            .border(1.dp, Color(0xFF2D3244), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            map.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Tier / Badge with Gold Highlight
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF111827))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        badge.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color(0xFFFFD700),
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Match Title & Time
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    title,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.White,
                    letterSpacing = (-0.3).sp,
                    lineHeight = 23.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        time,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Stats Grid (Prize Pool, Entry Fee, Slots Progress)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1D27))
                    .border(1.dp, Color(0xFF2E3346), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prize
                    Column {
                        Text(
                            "PRIZE POOL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            prize,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(26.dp)
                            .background(Color(0xFF2E3346))
                    )

                    // Entry Fee
                    Column {
                        Text(
                            "ENTRY FEE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            entry,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = if (entry.contains("Free", ignoreCase = true)) Color(0xFF10B981) else Color.White
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(26.dp)
                            .background(Color(0xFF2E3346))
                    )

                    // Slots Left
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "PLAYERS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "$slotsBooked/$totalSlots",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Bottom Action Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Micro Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Free Fire Scrim",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // High-End Join / Watch / IDP Action Button with Floating Elevation
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isLive -> Color(0xFFFF003C)
                            isCompleted -> Color(0xFF1E1E24)
                            else -> Color.White
                        },
                        contentColor = when {
                            isLive -> Color.White
                            isCompleted -> Color.White
                            else -> Color.Black
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isLive -> Icons.Default.PlayArrow
                                isCompleted -> Icons.Default.EmojiEvents
                                else -> Icons.Default.Bolt
                            },
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = when {
                                isLive -> Color.White
                                isCompleted -> Color(0xFFFFD700)
                                else -> Color.Black
                            }
                        )
                        Text(
                            text = when {
                                isLive -> "WATCH / IDP"
                                isCompleted -> "RESULTS"
                                else -> "JOIN MATCH"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
