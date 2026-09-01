package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
    val cleanEntry = entry.replace("🎬", "").replace("💎", "").replace("🔥", "").trim()
    val cleanBadge = badge.replace("🎬", "").replace("💎", "").replace("🔥", "").trim()

    // ✨ Infinite Transition for Silver & Diamond Sheen Animation
    val infiniteTransition = rememberInfiniteTransition(label = "SilverDiamondAnim")
    
    // Shimmer position moving across card
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -250f,
        targetValue = 950f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerSweep"
    )

    // Subtle breathing diamond glow
    val diamondGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "diamondGlow"
    )

    // Silver & Diamond Border Gradient
    val silverDiamondBorder = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0).copy(alpha = diamondGlowAlpha), // Platinum Silver
            Color(0xFF80DEEA).copy(alpha = diamondGlowAlpha * 0.85f), // Diamond Ice Blue
            Color(0xFF334155), // Deep Titanium Steel
            Color(0xFFE2E8F0).copy(alpha = diamondGlowAlpha * 0.65f), // Silver Reflection
            Color(0xFF00E5FF).copy(alpha = diamondGlowAlpha * 0.55f)  // Diamond Cyan
        ),
        start = Offset(0f, 0f),
        end = Offset(800f, 800f)
    )

    // Shimmer highlight brush
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color(0xFFE2E8F0).copy(alpha = 0.03f),
            Color(0xFF80DEEA).copy(alpha = 0.10f), // Diamond flash
            Color(0xFFFFFFFF).copy(alpha = 0.14f), // Silver flash
            Color(0xFF80DEEA).copy(alpha = 0.06f),
            Color.Transparent
        ),
        start = Offset(shimmerTranslate - 150f, shimmerTranslate - 150f),
        end = Offset(shimmerTranslate + 150f, shimmerTranslate + 150f)
    )

    // Compact Floating card container with soft 3D ambient shadow & silver-diamond edge
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0x2600E5FF),
                ambientColor = Color(0x1F000000)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F131D), // Dark titanium obsidian
                        Color(0xFF141926), // Deep blue-gray metallic
                        Color(0xFF0D1017)  // Pure deep steel
                    )
                )
            )
            .border(1.2.dp, silverDiamondBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        // ✨ Subtle Animated Diamond-Silver Shimmer Overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shimmerBrush)
        )

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            // Header Row: Map & Status Indicator + Diamond Mode Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isLive -> Color(0xFFFF003C)
                                    isCompleted -> Color(0xFF00C853)
                                    else -> Color(0xFF1E293B)
                                }
                            )
                            .border(
                                0.8.dp,
                                when {
                                    isLive -> Color(0xFFFF5252)
                                    isCompleted -> Color(0xFF69F0AE)
                                    else -> Color(0xFF475569)
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isLive) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                if (isLive) "● LIVE" else if (isCompleted) "FINISHED" else "UPCOMING",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Map Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E2433))
                            .border(0.8.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            map.uppercase(),
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Black,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                // Tier / Badge with Diamond-Silver Highlight
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(
                            0.8.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF80DEEA), Color(0xFFE2E8F0), Color(0xFFFFD700))
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        cleanBadge.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color(0xFFE2E8F0),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Match Title & Time (Compact & Sharp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color.White,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 20.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        time,
                        fontSize = 11.5.sp,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Stats Grid (Prize Pool, Entry Fee, Slots Progress) - Compact Frosted Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161B26))
                    .border(
                        0.8.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF334155).copy(alpha = 0.7f),
                                Color(0xFF80DEEA).copy(alpha = 0.25f),
                                Color(0xFF334155).copy(alpha = 0.7f)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
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
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            prize,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFFFFD700)
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color(0xFF334155))
                    )

                    // Entry Fee
                    Column {
                        Text(
                            "ENTRY FEE",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            cleanEntry,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (cleanEntry.contains("Free", ignoreCase = true)) Color(0xFF00E676) else Color(0xFFE2E8F0)
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color(0xFF334155))
                    )

                    // Slots Left
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "PLAYERS",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            "$slotsBooked/$totalSlots",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF80DEEA)
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
                        tint = Color(0xFF80DEEA),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        "Free Fire Scrim",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }

                // 💎 Crisp Clean Join Button (Glitch-Free & Sharp)
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isLive -> Color(0xFFFF003C)
                            isCompleted -> Color(0xFF1E2433)
                            else -> Color.White
                        },
                        contentColor = when {
                            isLive -> Color.White
                            isCompleted -> Color(0xFFE2E8F0)
                            else -> Color.Black
                        }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isLive -> Color(0xFFFF5252)
                            isCompleted -> Color(0xFF475569)
                            else -> Color(0xFF80DEEA)
                        }
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isLive -> Icons.Default.PlayArrow
                                isCompleted -> Icons.Default.EmojiEvents
                                else -> Icons.Default.Bolt
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
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
                            fontSize = 11.5.sp,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }
        }
    }
}
