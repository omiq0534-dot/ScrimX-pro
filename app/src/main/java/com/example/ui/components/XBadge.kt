package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class XBadgeSize {
    MINI,    // In-line with player names in slots / tables (20dp)
    NORMAL,  // In profile and leaderboard headers (28dp)
    LARGE    // Hero profile showcase (46dp)
}

enum class BadgeGemStyle {
    RUBY_PRO_X,       // Blazing Faceted Esports Ruby [X]
    OWNER_CROWN,      // Imperial Gold & Diamond Ruby [👑 OWNER]
    MOD_SHIELD        // Royal Amethyst & Cobalt Sapphire [🛡️ MOD]
}

/**
 * 💎 Ultra-Realistic 3D Faceted Crystal Gemstone Badge
 * Real Mineral Optics:
 * - Geometric 8-Faceted Brilliant Cut with directional light refraction
 * - Zero outside overflow: 100% strictly clipped to the crystal facet geometry
 * - Internal Glass Sheen & Caustic Depth (Bright top reflection + Deep wine crimson heart + Dark shadow pavilion)
 * - Micro Diamond Twinkle Star at the top-left crown facet (No thick lines)
 * - Laser-Embedded Emblem (👑 Crown, 🛡️ Shield, ⚡ [X])
 */
@Composable
fun GemstoneBadge(
    style: BadgeGemStyle,
    modifier: Modifier = Modifier,
    size: XBadgeSize = XBadgeSize.NORMAL,
    isAnimated: Boolean = true,
    showClickInfo: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "GemSparkle")
    
    // Soft breathing pulse
    val pulseScale by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    // Micro Star Diamond Twinkle flare (0.2f to 1f)
    val sparkleIntensity by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sparkle"
        )
    } else {
        remember { mutableFloatStateOf(0.7f) }
    }

    // Internal crystal refraction shimmer (strictly inside gem)
    val shimmerPhase by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = -0.3f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
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

    val iconDimension = when (size) {
        XBadgeSize.MINI -> 10.dp
        XBadgeSize.NORMAL -> 14.dp
        XBadgeSize.LARGE -> 22.dp
    }

    // Outer Bezel Rim Palette
    val rimColors = when (style) {
        BadgeGemStyle.RUBY_PRO_X -> listOf(
            Color(0xFFFFF7C2), // Sparkling Gold
            Color(0xFFFFD700), // Pure 24K Gold
            Color(0xFFFF9100), // Amber Flare
            Color(0xFFFF1744), // Crimson
            Color(0xFF6B000B)  // Shadow Rim
        )
        BadgeGemStyle.OWNER_CROWN -> listOf(
            Color(0xFFFFFFFF), // Platinum White
            Color(0xFFFFEA00), // Royal Gold
            Color(0xFFFF0055), // Imperial Ruby
            Color(0xFFFFD700), // 24K Gold
            Color(0xFF4A000A)  // Deep Red Shadow
        )
        BadgeGemStyle.MOD_SHIELD -> listOf(
            Color(0xFFFFFFFF), // Ice White
            Color(0xFF818CF8), // Electric Indigo
            Color(0xFF4F46E5), // Cobalt Blue
            Color(0xFF00E5FF), // Cyan Sheen
            Color(0xFF1E1B4B)  // Deep Sapphire Shadow
        )
    }

    // Facet Palette
    val topFacetColor = when (style) {
        BadgeGemStyle.RUBY_PRO_X -> Color(0xFFFF80AB).copy(alpha = 0.65f)
        BadgeGemStyle.OWNER_CROWN -> Color(0xFFFF80AB).copy(alpha = 0.75f)
        BadgeGemStyle.MOD_SHIELD -> Color(0xFFA5B4FC).copy(alpha = 0.65f)
    }

    val leftFacetColor = when (style) {
        BadgeGemStyle.RUBY_PRO_X -> Color(0xFFFF4081).copy(alpha = 0.45f)
        BadgeGemStyle.OWNER_CROWN -> Color(0xFFFF4081).copy(alpha = 0.5f)
        BadgeGemStyle.MOD_SHIELD -> Color(0xFF818CF8).copy(alpha = 0.45f)
    }

    val bottomFacetColor = when (style) {
        BadgeGemStyle.RUBY_PRO_X -> Color(0xFF330005).copy(alpha = 0.75f)
        BadgeGemStyle.OWNER_CROWN -> Color(0xFF280004).copy(alpha = 0.8f)
        BadgeGemStyle.MOD_SHIELD -> Color(0xFF0F172A).copy(alpha = 0.75f)
    }

    val tableCenterColors = when (style) {
        BadgeGemStyle.RUBY_PRO_X -> listOf(
            Color(0xFFFF1744), // Bright Ruby Center
            Color(0xFFD50000), // Deep Scarlet
            Color(0xFF70000C)  // Dark Mineral Base
        )
        BadgeGemStyle.OWNER_CROWN -> listOf(
            Color(0xFFFF2A55), // Imperial Crimson
            Color(0xFFB71C1C), // Royal Dark Scarlet
            Color(0xFF4D0008)  // Blood Diamond Shadow
        )
        BadgeGemStyle.MOD_SHIELD -> listOf(
            Color(0xFF6366F1), // Royal Amethyst
            Color(0xFF4338CA), // Deep Indigo
            Color(0xFF1E1B4B)  // Midnight Sapphire
        )
    }

    Box(
        modifier = modifier
            .scale(pulseScale)
            .size(badgeDimension)
            .clickable(enabled = showClickInfo) {
                if (showClickInfo) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDialog = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // High-Precision 3D Faceted Crystal Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cW = this.size.width
            val cH = this.size.height
            val cut = cW * 0.26f

            // 1. Outer Gem Bezel (Octagon)
            val outerPath = Path().apply {
                moveTo(cut, 0f)
                lineTo(cW - cut, 0f)
                lineTo(cW, cut)
                lineTo(cW, cH - cut)
                lineTo(cW - cut, cH)
                lineTo(cut, cH)
                lineTo(0f, cH - cut)
                lineTo(0f, cut)
                close()
            }

            // Draw Metallic Rim
            drawPath(
                path = outerPath,
                brush = Brush.linearGradient(
                    colors = rimColors,
                    start = Offset(0f, 0f),
                    end = Offset(cW, cH)
                ),
                style = Fill
            )

            // Inner Crystal Body
            val rimPad = cW * 0.07f
            val rimCut = (cW - (rimPad * 2f)) * 0.26f
            val gemPath = Path().apply {
                moveTo(rimPad + rimCut, rimPad)
                lineTo(cW - rimPad - rimCut, rimPad)
                lineTo(cW - rimPad, rimPad + rimCut)
                lineTo(cW - rimPad, cH - rimPad - rimCut)
                lineTo(cW - rimPad - rimCut, cH - rimPad)
                lineTo(rimPad + rimCut, cH - rimPad)
                lineTo(rimPad, cH - rimPad - rimCut)
                lineTo(rimPad, rimPad + rimCut)
                close()
            }

            // CRITICAL: Clip EVERYTHING inside the crystal body so NO light or line ever spills outside!
            clipPath(gemPath) {
                // A. Base Crystal Depth
                drawPath(
                    path = gemPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF3366),
                            Color(0xFFD50000),
                            Color(0xFF59000A)
                        )
                    ),
                    style = Fill
                )

                // B. Faceted Crown Bevels (3D Geometric Diamond Cut)
                val tablePad = cW * 0.22f
                val tableCut = (cW - (tablePad * 2f)) * 0.26f

                // Top Facet (High Glass Reflection)
                val topFacet = Path().apply {
                    moveTo(rimPad + rimCut, rimPad)
                    lineTo(cW - rimPad - rimCut, rimPad)
                    lineTo(cW - tablePad - tableCut, tablePad)
                    lineTo(tablePad + tableCut, tablePad)
                    close()
                }
                drawPath(path = topFacet, color = topFacetColor, style = Fill)

                // Left Facet (Ambient Light Sheen)
                val leftFacet = Path().apply {
                    moveTo(rimPad, rimPad + rimCut)
                    lineTo(rimPad + rimCut, rimPad)
                    lineTo(tablePad + tableCut, tablePad)
                    lineTo(tablePad, tablePad + tableCut)
                    lineTo(tablePad, cH - tablePad - tableCut)
                    lineTo(rimPad, cH - rimPad - rimCut)
                    close()
                }
                drawPath(path = leftFacet, color = leftFacetColor, style = Fill)

                // Right Facet (Mid-Shade Refraction)
                val rightFacet = Path().apply {
                    moveTo(cW - rimPad - rimCut, rimPad)
                    lineTo(cW - rimPad, rimPad + rimCut)
                    lineTo(cW - rimPad, cH - rimPad - rimCut)
                    lineTo(cW - tablePad, cH - tablePad - tableCut)
                    lineTo(cW - tablePad, tablePad + tableCut)
                    lineTo(cW - tablePad - tableCut, tablePad)
                    close()
                }
                drawPath(path = rightFacet, color = Color.Black.copy(alpha = 0.35f), style = Fill)

                // Bottom Facet (Deep Shadow Refraction)
                val bottomFacet = Path().apply {
                    moveTo(tablePad + tableCut, cH - tablePad)
                    lineTo(cW - tablePad - tableCut, cH - tablePad)
                    lineTo(cW - rimPad - rimCut, cH - rimPad)
                    lineTo(rimPad + rimCut, cH - rimPad)
                    close()
                }
                drawPath(path = bottomFacet, color = bottomFacetColor, style = Fill)

                // C. Flat Center Table (The Heart of the Crystal)
                val tablePath = Path().apply {
                    moveTo(tablePad + tableCut, tablePad)
                    lineTo(cW - tablePad - tableCut, tablePad)
                    lineTo(cW - tablePad, tablePad + tableCut)
                    lineTo(cW - tablePad, cH - tablePad - tableCut)
                    lineTo(cW - tablePad - tableCut, cH - tablePad)
                    lineTo(tablePad + tableCut, cH - tablePad)
                    lineTo(tablePad, cH - tablePad - tableCut)
                    lineTo(tablePad, tablePad + tableCut)
                    close()
                }

                drawPath(
                    path = tablePath,
                    brush = Brush.radialGradient(
                        colors = tableCenterColors,
                        center = Offset(cW * 0.42f, cH * 0.42f),
                        radius = cW * 0.45f
                    ),
                    style = Fill
                )

                // Razor-thin Table Border Accent
                drawPath(
                    path = tablePath,
                    color = Color.White.copy(alpha = 0.25f),
                    style = Stroke(width = 1f)
                )

                // D. Smooth Glass Specular Sheen (Curved Upper-Left Highlight)
                val glassGlare = Path().apply {
                    moveTo(rimPad, rimPad + rimCut)
                    lineTo(rimPad + rimCut, rimPad)
                    lineTo(cW * 0.55f, rimPad)
                    lineTo(rimPad, cH * 0.55f)
                    close()
                }
                drawPath(
                    path = glassGlare,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        start = Offset(rimPad, rimPad),
                        end = Offset(cW * 0.45f, cH * 0.45f)
                    ),
                    style = Fill
                )

                // E. Delicate Internal Shimmer (Hairline sweep inside the table only)
                if (isAnimated && shimmerPhase in 0f..1f) {
                    val sx = shimmerPhase * cW
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.45f),
                                Color(0xFFFFD700).copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            startX = sx - (cW * 0.15f),
                            endX = sx + (cW * 0.15f)
                        ),
                        start = Offset(sx, tablePad),
                        end = Offset(sx - (cW * 0.15f), cH - tablePad),
                        strokeWidth = 1.2f
                    )
                }

                // F. Micro Diamond Starburst Sparkle (Twinkles at top-left crown facet point)
                val starCenterX = tablePad + (tableCut * 0.5f)
                val starCenterY = tablePad * 0.9f
                val starRadius = (cW * 0.12f) * sparkleIntensity

                // Horizontal Ray
                drawLine(
                    color = Color.White.copy(alpha = 0.9f * sparkleIntensity),
                    start = Offset(starCenterX - starRadius, starCenterY),
                    end = Offset(starCenterX + starRadius, starCenterY),
                    strokeWidth = 1.2f,
                    cap = StrokeCap.Round
                )
                // Vertical Ray
                drawLine(
                    color = Color.White.copy(alpha = 0.9f * sparkleIntensity),
                    start = Offset(starCenterX, starCenterY - starRadius),
                    end = Offset(starCenterX, starCenterY + starRadius),
                    strokeWidth = 1.2f,
                    cap = StrokeCap.Round
                )
                // Center Star Point Glow
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f * sparkleIntensity),
                    radius = starRadius * 0.35f,
                    center = Offset(starCenterX, starCenterY)
                )
            }
        }

        // Center Laser-Inscribed Emblem
        when (style) {
            BadgeGemStyle.RUBY_PRO_X -> {
                Text(
                    text = "X",
                    color = Color(0xFF0A0C14), // Sharp Carbon Laser Black
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-1.5).sp,
                    modifier = Modifier.offset(y = (-0.5).dp)
                )
            }
            BadgeGemStyle.OWNER_CROWN -> {
                Text(
                    text = "👑",
                    fontSize = fontSize,
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }
            BadgeGemStyle.MOD_SHIELD -> {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Mod",
                    tint = Color.White,
                    modifier = Modifier.size(iconDimension)
                )
            }
        }
    }

    if (showDialog) {
        val title = when (style) {
            BadgeGemStyle.RUBY_PRO_X -> "PRO VERIFIED PLAYER"
            BadgeGemStyle.OWNER_CROWN -> "SUPREME OWNER BADGE"
            BadgeGemStyle.MOD_SHIELD -> "OFFICIAL MODERATOR"
        }
        val desc = when (style) {
            BadgeGemStyle.RUBY_PRO_X -> "This player is a verified Pro Tier Esports contender. Granted to elite champions and tournament finalists."
            BadgeGemStyle.OWNER_CROWN -> "The highest rank in ScrimX Esports. App Owner and Supreme Administrator."
            BadgeGemStyle.MOD_SHIELD -> "Official ScrimX Tournament Staff member authorized to manage lobbies and disputes."
        }
        val badgeColor = when (style) {
            BadgeGemStyle.RUBY_PRO_X -> Color(0xFFFF0055)
            BadgeGemStyle.OWNER_CROWN -> Color(0xFFFFD700)
            BadgeGemStyle.MOD_SHIELD -> Color(0xFF6366F1)
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                GemstoneBadge(style = style, size = XBadgeSize.LARGE, isAnimated = true, showClickInfo = false)
            },
            title = {
                Text(title, fontWeight = FontWeight.Black, color = Color.White, fontSize = 17.sp)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        desc,
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(badgeColor, Color(0xFFFFD700), Color.Transparent)),
                            width = 1.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AUTHENTIC VERIFIED CREDENTIAL",
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = badgeColor)
                ) {
                    Text("CLOSE", color = if (style == BadgeGemStyle.OWNER_CROWN) Color.Black else Color.White, fontWeight = FontWeight.Black)
                }
            },
            containerColor = Color(0xFF0F111A),
            tonalElevation = 12.dp,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Backward-compatible Wrapper for XBadge
 */
@Composable
fun XBadge(
    modifier: Modifier = Modifier,
    size: XBadgeSize = XBadgeSize.NORMAL,
    isAnimated: Boolean = true,
    showClickInfo: Boolean = false
) {
    GemstoneBadge(
        style = BadgeGemStyle.RUBY_PRO_X,
        modifier = modifier,
        size = size,
        isAnimated = isAnimated,
        showClickInfo = showClickInfo
    )
}

/**
 * Backward-compatible Wrapper for Admin / Owner Badge
 */
@Composable
fun AdminMasterBadge(
    isOwner: Boolean,
    modifier: Modifier = Modifier,
    size: XBadgeSize = XBadgeSize.NORMAL,
    showClickInfo: Boolean = false
) {
    GemstoneBadge(
        style = if (isOwner) BadgeGemStyle.OWNER_CROWN else BadgeGemStyle.MOD_SHIELD,
        modifier = modifier,
        size = size,
        isAnimated = true,
        showClickInfo = showClickInfo
    )
}

@Composable
fun FounderDiamondBadge(
    modifier: Modifier = Modifier,
    size: XBadgeSize = XBadgeSize.NORMAL,
    showClickInfo: Boolean = false
) {
    AdminMasterBadge(isOwner = true, modifier = modifier, size = size, showClickInfo = showClickInfo)
}
