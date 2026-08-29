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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

enum class XBadgeSize {
    MINI,    // In-line with player names in slots / tables (18dp)
    NORMAL,  // In profile and leaderboard headers (28dp)
    LARGE    // Hero profile showcase (44dp)
}

/**
 * ⚡ Ultra-Premium 3D Glassy Ruby Gemstone [X] Badge
 * Inspired by Kick's Iconic Polygon & Free Fire's [V] Badge, elevated to a luxury 3D faceted crystal ruby gem.
 * Featuring:
 * - 8-Sided Faceted Octagonal Crystal Cut with 3D Light Refraction
 * - Glossy Glass Specular Highlights & Diagonal Surface Glare
 * - Esports Crimson / Fiery Ruby Red (#FF003F -> #D50000 -> #880015)
 * - Metallic Golden Cyber Rim & Laser-Cut Deep Carbon [X]
 */
@Composable
fun XBadge(
    modifier: Modifier = Modifier,
    size: XBadgeSize = XBadgeSize.NORMAL,
    isAnimated: Boolean = true,
    showClickInfo: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "XBadgeGlint")
    
    // Smooth breathing pulse
    val pulseScale by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    // Moving glass gleam sweep
    val gleamOffset by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = -0.6f,
            targetValue = 1.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "gleam"
        )
    } else {
        remember { mutableFloatStateOf(-1f) }
    }

    val badgeDimension = when (size) {
        XBadgeSize.MINI -> 20.dp
        XBadgeSize.NORMAL -> 28.dp
        XBadgeSize.LARGE -> 46.dp
    }

    val fontSize: TextUnit = when (size) {
        XBadgeSize.MINI -> 11.sp
        XBadgeSize.NORMAL -> 15.sp
        XBadgeSize.LARGE -> 25.sp
    }

    Box(
        modifier = modifier
            .scale(pulseScale)
            .size(badgeDimension)
            .clickable(enabled = showClickInfo) {
                if (showClickInfo) showDialog = true
            },
        contentAlignment = Alignment.Center
    ) {
        // High-Precision 3D Glassy Faceted Ruby Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW: Float = this.size.width
            val canvasH: Float = this.size.height
            val cut: Float = canvasW * 0.26f

            // 1. Outer Octagon Path (The Gem Bezel)
            val outerPath = Path().apply {
                moveTo(cut, 0f)
                lineTo(canvasW - cut, 0f)
                lineTo(canvasW, cut)
                lineTo(canvasW, canvasH - cut)
                lineTo(canvasW - cut, canvasH)
                lineTo(cut, canvasH)
                lineTo(0f, canvasH - cut)
                lineTo(0f, cut)
                close()
            }

            // Outer Golden Cyber-Chamber Rim
            drawPath(
                path = outerPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFF4B8), // Top-left Gold Highlight
                        Color(0xFFFFD700), // Pure Gold
                        Color(0xFFFF6D00), // Amber Flare
                        Color(0xFFFF1744), // Crimson
                        Color(0xFF880015)  // Deep Shadow Gold/Bronze
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(canvasW, canvasH)
                ),
                style = Fill
            )

            // 2. Inner Crystal Base (Deep Glowing Ruby Body)
            val rimPad: Float = canvasW * 0.065f
            val rimCut: Float = (canvasW - (rimPad * 2f)) * 0.26f
            val gemPath = Path().apply {
                moveTo(rimPad + rimCut, rimPad)
                lineTo(canvasW - rimPad - rimCut, rimPad)
                lineTo(canvasW - rimPad, rimPad + rimCut)
                lineTo(canvasW - rimPad, canvasH - rimPad - rimCut)
                lineTo(canvasW - rimPad - rimCut, canvasH - rimPad)
                lineTo(rimPad + rimCut, canvasH - rimPad)
                lineTo(rimPad, canvasH - rimPad - rimCut)
                lineTo(rimPad, rimPad + rimCut)
                close()
            }

            drawPath(
                path = gemPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF4081), // Top Light Ruby
                        Color(0xFFFF003F), // Neon Crimson
                        Color(0xFFD50000), // Deep Scarlet
                        Color(0xFF6B000B)  // Shadow Ruby Red
                    )
                ),
                style = Fill
            )

            // 3. Faceted Crystal 3D Depth (Top bevels reflect bright light, bottom in rich shade)
            val tablePad: Float = canvasW * 0.20f
            val tableCut: Float = (canvasW - (tablePad * 2f)) * 0.26f

            // Top Facet (High Reflection)
            val topFacet = Path().apply {
                moveTo(rimPad + rimCut, rimPad)
                lineTo(canvasW - rimPad - rimCut, rimPad)
                lineTo(canvasW - tablePad - tableCut, tablePad)
                lineTo(tablePad + tableCut, tablePad)
                close()
            }
            drawPath(
                path = topFacet,
                color = Color.White.copy(alpha = 0.35f),
                style = Fill
            )

            // Left Facet (Medium Reflection)
            val leftFacet = Path().apply {
                moveTo(rimPad, rimPad + rimCut)
                lineTo(rimPad + rimCut, rimPad)
                lineTo(tablePad + tableCut, tablePad)
                lineTo(tablePad, tablePad + tableCut)
                lineTo(tablePad, canvasH - tablePad - tableCut)
                lineTo(rimPad, canvasH - rimPad - rimCut)
                close()
            }
            drawPath(
                path = leftFacet,
                color = Color.White.copy(alpha = 0.18f),
                style = Fill
            )

            // Bottom & Right Facets (Deep Shaded Glass Depth)
            val bottomFacet = Path().apply {
                moveTo(tablePad + tableCut, canvasH - tablePad)
                lineTo(canvasW - tablePad - tableCut, canvasH - tablePad)
                lineTo(canvasW - rimPad - rimCut, canvasH - rimPad)
                lineTo(rimPad + rimCut, canvasH - rimPad)
                close()
            }
            drawPath(
                path = bottomFacet,
                color = Color.Black.copy(alpha = 0.45f),
                style = Fill
            )

            // 4. Center Table Gemstone Flat
            val tablePath = Path().apply {
                moveTo(tablePad + tableCut, tablePad)
                lineTo(canvasW - tablePad - tableCut, tablePad)
                lineTo(canvasW - tablePad, tablePad + tableCut)
                lineTo(canvasW - tablePad, canvasH - tablePad - tableCut)
                lineTo(canvasW - tablePad - tableCut, canvasH - tablePad)
                lineTo(tablePad + tableCut, canvasH - tablePad)
                lineTo(tablePad, canvasH - tablePad - tableCut)
                lineTo(tablePad, tablePad + tableCut)
                close()
            }

            drawPath(
                path = tablePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF3366), // Inner glowing core
                        Color(0xFFD50000), // Crimson Red
                        Color(0xFF880015)  // Deep rich ruby
                    ),
                    center = Offset(canvasW * 0.4f, canvasH * 0.4f),
                    radius = canvasW * 0.45f
                ),
                style = Fill
            )

            // Table Border Accent Line
            drawPath(
                path = tablePath,
                color = Color(0xFFFFD700).copy(alpha = 0.4f),
                style = Stroke(width = 1f)
            )

            // 5. Glossy Glass Specular Sheen (Curved Glass Sheen on Top-Left)
            val glassGlare = Path().apply {
                moveTo(rimPad, rimPad + rimCut)
                lineTo(rimPad + rimCut, rimPad)
                lineTo(canvasW * 0.65f, rimPad)
                lineTo(rimPad, canvasH * 0.65f)
                close()
            }
            drawPath(
                path = glassGlare,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(rimPad, rimPad),
                    end = Offset(canvasW * 0.5f, canvasH * 0.5f)
                ),
                style = Fill
            )

            // 6. Animated Light Glint Ray
            if (isAnimated && gleamOffset in 0f..1f) {
                val gx = gleamOffset * canvasW
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.65f),
                            Color(0xFFFFE082).copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.65f),
                            Color.Transparent
                        ),
                        startX = gx - (canvasW * 0.2f),
                        endX = gx + (canvasW * 0.2f)
                    ),
                    start = Offset(gx, 0f),
                    end = Offset(gx - (canvasW * 0.3f), canvasH),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }

        // Heavy Bold Pixel-Cut "X" (Esports Laser-Cut Style in Center of Gem)
        Text(
            text = "X",
            color = Color(0xFF07090E), // Ultra-Deep Laser Solid Black
            fontWeight = FontWeight.Black,
            fontSize = fontSize,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-1.5).sp,
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
                if (isOwner) "👑" else "🛡️",
                fontSize = 11.sp
            )
            Text(
                text = if (isOwner) "OWNER" else "MODERATOR",
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
