package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class XBadgeSize {
    MINI,    // In-line with player names in slots / tables (16-18dp)
    NORMAL,  // In profile and leaderboard headers (24-28dp)
    LARGE    // Hero profile showcase (40-48dp)
}

enum class BadgeType {
    X_BADGE,       // Gold/Fire [X] Pro Player Badge
    ADMIN_BADGE,   // Ruby Electric Crown [ADMIN] Badge
    FOUNDER_BADGE  // Diamond/Gold [OWNER] Badge
}

/**
 * 👑 Official ScrimX [X] Badge Component
 * Inspired by Free Fire's [V] Badge & Kick's [K] Badge.
 * Rendered with golden-fiery metallic gradients and diamond/hexagon styling.
 */
@Composable
fun XBadge(
    modifier: Modifier = Modifier,
    size: XBadgeSize = XBadgeSize.NORMAL,
    isAnimated: Boolean = true,
    showClickInfo: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    // Shimmer/Pulse animation for live shine
    val infiniteTransition = rememberInfiniteTransition(label = "XBadgeShine")
    val pulseScale by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    val badgeDimension = when (size) {
        XBadgeSize.MINI -> 18.dp
        XBadgeSize.NORMAL -> 26.dp
        XBadgeSize.LARGE -> 44.dp
    }

    val fontSize: TextUnit = when (size) {
        XBadgeSize.MINI -> 10.sp
        XBadgeSize.NORMAL -> 15.sp
        XBadgeSize.LARGE -> 24.sp
    }

    val cornerRadius = when (size) {
        XBadgeSize.MINI -> 5.dp
        XBadgeSize.NORMAL -> 7.dp
        XBadgeSize.LARGE -> 12.dp
    }

    Box(
        modifier = modifier
            .scale(pulseScale)
            .size(badgeDimension)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFD700), // Bright Gold
                        Color(0xFFFF9100), // Amber
                        Color(0xFFFF003F)  // Ruby ScrimX Red
                    )
                )
            )
            .border(
                width = if (size == XBadgeSize.LARGE) 2.dp else 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFE082),
                        Color(0xFFFF3D00)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(enabled = showClickInfo) {
                if (showClickInfo) showDialog = true
            },
        contentAlignment = Alignment.Center
    ) {
        // High-contrast Bold stylized 'X'
        Text(
            text = "X",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = fontSize,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.offset(y = (-0.5).dp)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF10131E),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    XBadge(size = XBadgeSize.NORMAL, isAnimated = false)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "VERIFIED [X] BADGE",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "This player is an Officially Verified [X] ScrimX Champion & Partner! 👑",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "• Awarded to Top Tournament Champions (10+ Wins)\n• Verified Esports Creators & Streamers\n• ScrimX Elite VIP Members",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("AWESOME", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        )
    }
}

/**
 * 👑 Supreme [ADMIN / OWNER] Master Crown Badge
 * Exclusive for Admin & App Owner
 */
@Composable
fun AdminMasterBadge(
    modifier: Modifier = Modifier,
    isOwner: Boolean = true,
    showClickInfo: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "AdminBadgePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "adminPulse"
    )

    Box(
        modifier = modifier
            .scale(pulseScale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    if (isOwner) listOf(Color(0xFFFF0055), Color(0xFFFF5252), Color(0xFFFFD700))
                    else listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFFFD700))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = showClickInfo) {
                if (showClickInfo) showDialog = true
            }
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                "👑",
                fontSize = 11.sp
            )
            Text(
                text = if (isOwner) "OWNER" else "ADMIN",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF10131E),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isOwner) "👑 SUPREME APP OWNER" else "🛡️ VERIFIED ADMIN",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (isOwner)
                            "Official Founder & System Creator of ScrimX Esports! 🚀"
                        else
                            "Verified Tournament Moderator & Security Administrator 🛡️",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        "• Full Server Database Control\n• Anti-Cheat & Ban Authority\n• Official Match & Prize Management",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055))
                ) {
                    Text("CLOSE", color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        )
    }
}

/**
 * Player Name Row with optional [X] Badge
 */
@Composable
fun PlayerNameWithXBadge(
    name: String,
    hasXBadge: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    badgeSize: XBadgeSize = XBadgeSize.MINI
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasXBadge) {
            XBadge(size = badgeSize, showClickInfo = true)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = name,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            maxLines = 1
        )
    }
}
