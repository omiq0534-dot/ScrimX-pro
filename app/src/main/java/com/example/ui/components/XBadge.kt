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

    // Micro Star Diamond Twinkle flare
    val sparkleIntensity by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 3600
                    0f at 0
                    0.2f at 400
                    1f at 650 using FastOutSlowInEasing
                    0f at 1100 using FastOutSlowInEasing
                    0f at 3600
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "sparkle"
        )
    } else {
        remember { mutableFloatStateOf(0.7f) }
    }

    // Internal crystal refraction shimmer with crisp speed & delay pause
    val shimmerPhase by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = -0.3f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 3600
                    -0.3f at 0
                    1.3f at 650 using FastOutSlowInEasing
                    1.3f at 3600
                },
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
            Color(0xFFFFFDF0), // Platinum Light Top
            Color(0xFFFFDF79), // 24K Champagne Gold
            Color(0xFFFFB300), // Amber Gold
            Color(0xFFFF1744), // Ruby Reflection
            Color(0xFF5A2A00)  // Deep Bronze Shadow Rim
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
        // High-Precision 3D Faceted Crystal Canvas with Authentic Ruby Diamond Physics
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cW = this.size.width
            val cH = this.size.height
            val cx = cW * 0.5f
            val cy = cH * 0.5f

            // ====================================================================
            // 0. RADIENT AURA GLOW (Intense Luminous Gemstone Corona)
            // ====================================================================
            if (style == BadgeGemStyle.RUBY_PRO_X || style == BadgeGemStyle.OWNER_CROWN) {
                // Wide ambient fiery bloom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF1744).copy(alpha = 0.52f * pulseScale),
                            Color(0xFFFF003C).copy(alpha = 0.35f * pulseScale),
                            Color(0xFFD50000).copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = cW * 0.52f
                    ),
                    radius = cW * 0.52f,
                    center = Offset(cx, cy)
                )
                // Core hot optical flare
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF5277).copy(alpha = 0.35f * pulseScale),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = cW * 0.38f
                    ),
                    radius = cW * 0.38f,
                    center = Offset(cx, cy)
                )
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = 0.50f * pulseScale),
                            Color(0xFF00E5FF).copy(alpha = 0.25f * pulseScale),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = cW * 0.50f
                    ),
                    radius = cW * 0.50f,
                    center = Offset(cx, cy)
                )
            }

            // ====================================================================
            // 1. OUTER GOLD & PLATINUM BEZEL (Octagon Jewellery Setting)
            // ====================================================================
            val pad = cW * 0.06f
            val minX = pad
            val minY = pad
            val maxX = cW - pad
            val maxY = cH - pad
            val gW = maxX - minX
            val gH = maxY - minY
            val cut = gW * 0.26f

            val outerBezelPath = Path().apply {
                moveTo(minX + cut, minY)
                lineTo(maxX - cut, minY)
                lineTo(maxX, minY + cut)
                lineTo(maxX, maxY - cut)
                lineTo(maxX - cut, maxY)
                lineTo(minX + cut, maxY)
                lineTo(minX, maxY - cut)
                lineTo(minX, minY + cut)
                close()
            }

            // Draw Beveled Metallic Setting
            drawPath(
                path = outerBezelPath,
                brush = Brush.linearGradient(
                    colors = rimColors,
                    start = Offset(minX, minY),
                    end = Offset(maxX, maxY)
                ),
                style = Fill
            )

            // Inner Shadow Edge for the Bezel Rim
            drawPath(
                path = outerBezelPath,
                color = Color.Black.copy(alpha = 0.45f),
                style = Stroke(width = 1.2f)
            )

            // ====================================================================
            // 2. INNER CRYSTAL BODY (Brilliant-Cut Faceted Ruby Mineral)
            // ====================================================================
            val rimPad = gW * 0.065f
            val rMinX = minX + rimPad
            val rMinY = minY + rimPad
            val rMaxX = maxX - rimPad
            val rMaxY = maxY - rimPad
            val rCut = (rMaxX - rMinX) * 0.26f

            val gemGirdlePath = Path().apply {
                moveTo(rMinX + rCut, rMinY)
                lineTo(rMaxX - rCut, rMinY)
                lineTo(rMaxX, rMinY + rCut)
                lineTo(rMaxX, rMaxY - rCut)
                lineTo(rMaxX - rCut, rMaxY)
                lineTo(rMinX + rCut, rMaxY)
                lineTo(rMinX, rMaxY - rCut)
                lineTo(rMinX, rMinY + rCut)
                close()
            }

            // Clip ALL internal crystal optics strictly to the gem body
            clipPath(gemGirdlePath) {
                // A. Base Crystal Pavilion Depth (Deep Pigeon Blood Ruby)
                val crystalBaseColors = when (style) {
                    BadgeGemStyle.RUBY_PRO_X -> listOf(
                        Color(0xFFFF2A6D), // Hot electric ruby flare
                        Color(0xFFFF0844), // Vivid radiant crimson
                        Color(0xFFB30021), // Pure deep royal ruby
                        Color(0xFF5A0010), // Velvet burgundy
                        Color(0xFF1E0005)  // Deepest optical absorption
                    )
                    BadgeGemStyle.OWNER_CROWN -> listOf(
                        Color(0xFFFF3366),
                        Color(0xFFD50000),
                        Color(0xFF800012),
                        Color(0xFF280004)
                    )
                    BadgeGemStyle.MOD_SHIELD -> listOf(
                        Color(0xFF818CF8),
                        Color(0xFF4F46E5),
                        Color(0xFF312E81),
                        Color(0xFF0F172A)
                    )
                }

                drawPath(
                    path = gemGirdlePath,
                    brush = Brush.radialGradient(
                        colors = crystalBaseColors,
                        center = Offset(cx - (gW * 0.08f), cy - (gH * 0.10f)),
                        radius = gW * 0.70f
                    ),
                    style = Fill
                )

                // ================================================================
                // B. BRILLIANT-CUT CROWN FACETS (3D Glass Geometric Refraction)
                // ================================================================
                val tablePad = gW * 0.22f
                val tMinX = minX + tablePad
                val tMinY = minY + tablePad
                val tMaxX = maxX - tablePad
                val tMaxY = maxY - tablePad
                val tCut = (tMaxX - tMinX) * 0.26f

                // 8 Girdle Outer Vertices
                val g1 = Offset(rMinX + rCut, rMinY)
                val g2 = Offset(rMaxX - rCut, rMinY)
                val g3 = Offset(rMaxX, rMinY + rCut)
                val g4 = Offset(rMaxX, rMaxY - rCut)
                val g5 = Offset(rMaxX - rCut, rMaxY)
                val g6 = Offset(rMinX + rCut, rMaxY)
                val g7 = Offset(rMinX, rMaxY - rCut)
                val g8 = Offset(rMinX, rMinY + rCut)

                // 8 Table Inner Vertices
                val t1 = Offset(tMinX + tCut, tMinY)
                val t2 = Offset(tMaxX - tCut, tMinY)
                val t3 = Offset(tMaxX, tMinY + tCut)
                val t4 = Offset(tMaxX, tMaxY - tCut)
                val t5 = Offset(tMaxX - tCut, tMaxY)
                val t6 = Offset(tMinX + tCut, tMaxY)
                val t7 = Offset(tMinX, tMaxY - tCut)
                val t8 = Offset(tMinX, tMinY + tCut)

                // Helper to draw a facet polygon
                fun drawFacet(p1: Offset, p2: Offset, p3: Offset, p4: Offset, color: Color) {
                    val p = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        lineTo(p4.x, p4.y)
                        close()
                    }
                    drawPath(path = p, color = color, style = Fill)
                }

                // 1. Top Crown Facet (Receives direct ambient glass reflection)
                drawFacet(g1, g2, t2, t1, Color(0xFFFFB3C6).copy(alpha = 0.65f))
                // 2. Top-Right Facet (Specular angled light)
                drawFacet(g2, g3, t3, t2, Color(0xFFFF6B8B).copy(alpha = 0.50f))
                // 3. Right Facet (Mid-tone ruby)
                drawFacet(g3, g4, t4, t3, Color(0xFF48000A).copy(alpha = 0.55f))
                // 4. Bottom-Right Facet (Deep internal absorption shadow)
                drawFacet(g4, g5, t5, t4, Color(0xFF140003).copy(alpha = 0.88f))
                // 5. Bottom Facet (Dark pavilion shadow)
                drawFacet(g5, g6, t6, t5, Color(0xFF250006).copy(alpha = 0.82f))
                // 6. Bottom-Left Facet (Refracted ruby bounce)
                drawFacet(g6, g7, t7, t6, Color(0xFF6B0014).copy(alpha = 0.60f))
                // 7. Left Facet (Ambient crimson sheen)
                drawFacet(g7, g8, t8, t7, Color(0xFFFF3366).copy(alpha = 0.42f))
                // 8. Top-Left Facet (Primary glass flare facet)
                drawFacet(g8, g1, t1, t8, Color(0xFFFFD6E4).copy(alpha = 0.72f))

                // Fine diamond cut crease lines across all facet junctions
                val creaseColor = Color.White.copy(alpha = 0.38f)
                val creaseStroke = Stroke(width = 0.95f)
                drawLine(creaseColor, g1, t1, strokeWidth = creaseStroke.width)
                drawLine(creaseColor, g2, t2, strokeWidth = creaseStroke.width)
                drawLine(creaseColor, g3, t3, strokeWidth = creaseStroke.width)
                drawLine(creaseColor, g4, t4, strokeWidth = creaseStroke.width)
                drawLine(creaseColor, g5, t5, strokeWidth = creaseStroke.width)
                drawLine(creaseColor, g6, t6, strokeWidth = creaseStroke.width)
                drawLine(creaseColor, g7, t7, strokeWidth = creaseStroke.width)
                drawLine(creaseColor, g8, t8, strokeWidth = creaseStroke.width)

                // ================================================================
                // C. FLAT CENTER TABLE (The Polished Mirror Face of the Crystal)
                // ================================================================
                val tablePath = Path().apply {
                    moveTo(t1.x, t1.y)
                    lineTo(t2.x, t2.y)
                    lineTo(t3.x, t3.y)
                    lineTo(t4.x, t4.y)
                    lineTo(t5.x, t5.y)
                    lineTo(t6.x, t6.y)
                    lineTo(t7.x, t7.y)
                    lineTo(t8.x, t8.y)
                    close()
                }

                val tableCenterColors = when (style) {
                    BadgeGemStyle.RUBY_PRO_X -> listOf(
                        Color(0xFFFF1744), // Brilliant fiery ruby heart
                        Color(0xFFD50000), // Pure blood ruby
                        Color(0xFF7F0013)  // Deep table perimeter
                    )
                    BadgeGemStyle.OWNER_CROWN -> listOf(
                        Color(0xFFFF2A55),
                        Color(0xFFB71C1C),
                        Color(0xFF4D0008)
                    )
                    BadgeGemStyle.MOD_SHIELD -> listOf(
                        Color(0xFF6366F1),
                        Color(0xFF4338CA),
                        Color(0xFF1E1B4B)
                    )
                }

                drawPath(
                    path = tablePath,
                    brush = Brush.radialGradient(
                        colors = tableCenterColors,
                        center = Offset(cx, cy),
                        radius = gW * 0.36f
                    ),
                    style = Fill
                )

                // Razor-thin Table Rim Accent (Light Catching Table Edge)
                drawPath(
                    path = tablePath,
                    color = Color.White.copy(alpha = 0.45f),
                    style = Stroke(width = 1.1f)
                )

                // ================================================================
                // D. BOLD CHISELED ESPORTS BLACK [X] (100% Solid Black with Gold Bevel)
                //    - Custom geometric Esports gaming "X" glyph
                //    - Sharp predatory wing serifs at outer corners for elite gaming style
                //    - Solid Pitch-Black Carbon Fill + Polished 24K Gold Rim
                //    - Crisp and unmistakably readable as "X" in all sizes
                // ================================================================
                if (style == BadgeGemStyle.RUBY_PRO_X) {
                    val xReach = gW * 0.22f       // Total reach from center
                    val wingTip = gW * 0.055f      // Serif wing flare at tips
                    val strokeW = gW * 0.082f      // Main stem thickness
                    val waistW = gW * 0.065f       // Taper at center cross

                    // Build Diagonal Bar 1: Top-Left to Bottom-Right (\)
                    val slash1 = Path().apply {
                        // Top-Left Head with sharp aggressive serifs
                        moveTo(cx - xReach - wingTip, cy - xReach)
                        lineTo(cx - xReach + strokeW, cy - xReach)
                        lineTo(cx - strokeW * 0.4f, cy - waistW * 0.5f)
                        lineTo(cx - waistW * 0.5f, cy + strokeW * 0.4f)
                        // Bottom-Left inner
                        lineTo(cx + xReach - strokeW, cy + xReach)
                        lineTo(cx + xReach + wingTip, cy + xReach)
                        lineTo(cx + xReach, cy + xReach - strokeW)
                        lineTo(cx + strokeW * 0.4f, cy + waistW * 0.5f)
                        lineTo(cx + waistW * 0.5f, cy - strokeW * 0.4f)
                        lineTo(cx - xReach, cy - xReach + strokeW)
                        close()
                    }

                    // Build Diagonal Bar 2: Top-Right to Bottom-Left (/)
                    val slash2 = Path().apply {
                        // Top-Right Head
                        moveTo(cx + xReach + wingTip, cy - xReach)
                        lineTo(cx + xReach - strokeW, cy - xReach)
                        lineTo(cx + strokeW * 0.4f, cy - waistW * 0.5f)
                        lineTo(cx + waistW * 0.5f, cy + strokeW * 0.4f)
                        // Bottom-Left Head
                        lineTo(cx - xReach + strokeW, cy + xReach)
                        lineTo(cx - xReach - wingTip, cy + xReach)
                        lineTo(cx - xReach, cy + xReach - strokeW)
                        lineTo(cx - strokeW * 0.4f, cy + waistW * 0.5f)
                        lineTo(cx - waistW * 0.5f, cy - strokeW * 0.4f)
                        lineTo(cx + xReach, cy - xReach + strokeW)
                        close()
                    }

                    // 1. Deep Inset Drop Shadow into ruby crystal
                    drawPath(slash1, color = Color(0xCC0A0002), style = Fill)
                    drawPath(slash2, color = Color(0xCC0A0002), style = Fill)

                    // 2. High-Contrast 24K Gold & Platinum Outer Bevel Edge
                    val goldBevelBrush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF), // White-hot highlight gleam
                            Color(0xFFFFDF79), // 24K Champagne Gold
                            Color(0xFFFFB300), // Rich Imperial Gold
                            Color(0xFFFFD54F)  // Warm Gold
                        ),
                        start = Offset(cx - xReach, cy - xReach),
                        end = Offset(cx + xReach, cy + xReach)
                    )
                    drawPath(slash1, brush = goldBevelBrush, style = Stroke(width = 1.9f))
                    drawPath(slash2, brush = goldBevelBrush, style = Stroke(width = 1.9f))

                    // 3. 100% DEEP SOLID JET-BLACK (Obsidian Carbon Fill)
                    val pureBlackBrush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF181A20), // Subtle upper light reflection
                            Color(0xFF07080A), // Pure Jet Obsidian
                            Color(0xFF000000)  // Pitch Black
                        ),
                        start = Offset(cx, cy - xReach),
                        end = Offset(cx, cy + xReach)
                    )
                    drawPath(slash1, brush = pureBlackBrush, style = Fill)
                    drawPath(slash2, brush = pureBlackBrush, style = Fill)

                    // 4. Center Golden Diamond Stud in the Intersection
                    val coreR = strokeW * 0.46f
                    val centerStud = Path().apply {
                        moveTo(cx, cy - coreR)
                        lineTo(cx + coreR, cy)
                        lineTo(cx, cy + coreR)
                        lineTo(cx - coreR, cy)
                        close()
                    }
                    drawPath(
                        path = centerStud,
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFD700), Color(0xFFE69500)),
                            center = Offset(cx, cy),
                            radius = coreR
                        ),
                        style = Fill
                    )
                    drawPath(path = centerStud, color = Color.White, style = Stroke(width = 0.8f))
                    drawCircle(Color.White, radius = coreR * 0.35f, center = Offset(cx, cy))
                }

                // ================================================================
                // E. GLASSY DOME SHEEN & CAUSTICS (Authentic Real Ruby Glass Finish)
                // ================================================================

                // 1. Primary Curved Glass Lens Glare (Top-Left High Gloss Reflection)
                val glassGlare = Path().apply {
                    moveTo(rMinX, rMinY + rCut)
                    lineTo(rMinX + rCut, rMinY)
                    lineTo(cx + (gW * 0.20f), rMinY)
                    cubicTo(
                        cx - (gW * 0.04f), cy - (gH * 0.04f),
                        rMinX + (gW * 0.12f), cy + (gH * 0.06f),
                        rMinX, cy + (gH * 0.20f)
                    )
                    close()
                }
                drawPath(
                    path = glassGlare,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.72f),
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        start = Offset(rMinX, rMinY),
                        end = Offset(cx, cy)
                    ),
                    style = Fill
                )

                // 2. Secondary Bottom-Right Internal Reflection Arc (Glass thickness bounce)
                val bottomGlassArc = Path().apply {
                    moveTo(cx, rMaxY)
                    lineTo(rMaxX - rCut, rMaxY)
                    lineTo(rMaxX, rMaxY - rCut)
                    lineTo(rMaxX, cy)
                    cubicTo(
                        rMaxX - (gW * 0.08f), cy + (gH * 0.12f),
                        cx + (gW * 0.12f), rMaxY - (gH * 0.08f),
                        cx, rMaxY
                    )
                    close()
                }
                drawPath(
                    path = bottomGlassArc,
                    color = Color.White.copy(alpha = 0.14f),
                    style = Fill
                )

                // 3. Delicate Internal Shimmer Streak (Sweeping through the crystal)
                if (isAnimated && shimmerPhase in -0.2f..1.2f) {
                    clipPath(tablePath) {
                        val sx = shimmerPhase * cW
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.50f),
                                    Color(0xFFFFDF79).copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.50f),
                                    Color.Transparent
                                ),
                                startX = sx - (cW * 0.12f),
                                endX = sx + (cW * 0.12f)
                            ),
                            start = Offset(sx, 0f),
                            end = Offset(sx - (cW * 0.2f), cH),
                            strokeWidth = (cW * 0.065f).coerceAtLeast(1.5f)
                        )
                    }
                }

                // 4. Micro Diamond Starburst Sparkle (Twinkling star at top-left crown facet)
                if (isAnimated && sparkleIntensity > 0.05f) {
                    val starCenterX = tMinX + (tCut * 0.45f)
                    val starCenterY = tMinY * 0.95f
                    val starRadius = (gW * 0.11f) * sparkleIntensity

                    // Horizontal Ray
                    drawLine(
                        color = Color.White.copy(alpha = 0.95f * sparkleIntensity),
                        start = Offset(starCenterX - starRadius, starCenterY),
                        end = Offset(starCenterX + starRadius, starCenterY),
                        strokeWidth = 1.3f,
                        cap = StrokeCap.Round
                    )
                    // Vertical Ray
                    drawLine(
                        color = Color.White.copy(alpha = 0.95f * sparkleIntensity),
                        start = Offset(starCenterX, starCenterY - starRadius),
                        end = Offset(starCenterX, starCenterY + starRadius),
                        strokeWidth = 1.3f,
                        cap = StrokeCap.Round
                    )
                    // Micro 45-deg cross sparkle
                    val dR = starRadius * 0.5f
                    drawLine(
                        color = Color(0xFFFFF9C4).copy(alpha = 0.80f * sparkleIntensity),
                        start = Offset(starCenterX - dR, starCenterY - dR),
                        end = Offset(starCenterX + dR, starCenterY + dR),
                        strokeWidth = 0.9f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFFFF9C4).copy(alpha = 0.80f * sparkleIntensity),
                        start = Offset(starCenterX - dR, starCenterY + dR),
                        end = Offset(starCenterX + dR, starCenterY - dR),
                        strokeWidth = 0.9f,
                        cap = StrokeCap.Round
                    )
                    // Diamond point flare
                    drawCircle(
                        color = Color.White.copy(alpha = 0.90f * sparkleIntensity),
                        radius = starRadius * 0.35f,
                        center = Offset(starCenterX, starCenterY)
                    )
                }
            }
        }

        // Center Laser-Inscribed Emblem for OWNER and MOD (RUBY_PRO_X is drawn above in Canvas!)
        when (style) {
            BadgeGemStyle.RUBY_PRO_X -> {
                // Handled in high-performance 3D vector Canvas above! No standard keyboard font text!
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
