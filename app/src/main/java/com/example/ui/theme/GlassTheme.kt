package com.example.ui.theme
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.composed
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials

object GlassColors {
    val Background = Color(0xFFF1F5F9) // Off-white canvas
    
    // Typography
    val TextPrimary = Color(0xFF1E293B) // Slate 800
    val TextSecondary = Color(0xFF64748B) // Slate 500
    
    // Accents
    val GoldAccent = Color(0xFFF59E0B) // Amber/Gold
    val Positive = Color(0xFF10B981)
    val Negative = Color(0xFFEF4444)
    val Info = Color(0xFF06B6D4)

    val GlassSurface = Color.White.copy(alpha = 0.70f)
}

@Composable
fun WhiteGlassCard(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Transparent
            )
            .clip(RoundedCornerShape(cornerRadius))
            .hazeEffect(hazeState, style = HazeMaterials.ultraThin())
            .background(GlassColors.GlassSurface)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

val LocalHazeState = androidx.compose.runtime.compositionLocalOf<HazeState> { 
    error("No HazeState provided") 
}

@Composable
fun WhiteGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalHazeState.current ?: remember { HazeState() }
    WhiteGlassCard(
        hazeState = hazeState,
        modifier = modifier,
        cornerRadius = cornerRadius,
        content = content
    )
}

@Composable
fun GlassScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (HazeState) -> Unit
) {
    val hazeState = remember { HazeState() }
    
    androidx.compose.runtime.CompositionLocalProvider(LocalHazeState provides hazeState) {
        Box(modifier = modifier.background(GlassColors.Background).fillMaxSize()) {
            // Ambient background shapes
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().hazeSource(hazeState)) {
                // Draw soft pastel blobs for the glass to scatter
                val w = size.width
                val h = size.height
                
                // Top Right Blob (Soft Blue)
                drawCircle(
                    color = Color(0xFFE0F2FE).copy(alpha = 0.6f),
                    radius = w * 0.6f,
                    center = androidx.compose.ui.geometry.Offset(w, 0f)
                )
                
                // Bottom Left Blob (Soft Purple)
                drawCircle(
                    color = Color(0xFFF3E8FF).copy(alpha = 0.5f),
                    radius = w * 0.5f,
                    center = androidx.compose.ui.geometry.Offset(0f, h)
                )
                
                // Center Blob (Soft Amber)
                drawCircle(
                    color = Color(0xFFFEF3C7).copy(alpha = 0.4f),
                    radius = w * 0.4f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
                )
            }
            
            // Foreground Content
            content(hazeState)
        }
    }
}


fun Modifier.glassCard(
    cornerRadius: Dp = 24.dp,
    alpha: Float = 0.70f
): Modifier = composed {
    val hazeState = LocalHazeState.current ?: dev.chrisbanes.haze.HazeState()
    this
        .shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = Color.Black.copy(alpha = 0.04f),
            spotColor = Color.Transparent
        )
        .hazeEffect(hazeState, style = HazeMaterials.ultraThin())
        .background(Color.White.copy(alpha = alpha), RoundedCornerShape(cornerRadius))
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f),
                    Color.White.copy(alpha = 0.20f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}

fun Modifier.glassBackground(): Modifier = composed {
    val hazeState = LocalHazeState.current ?: dev.chrisbanes.haze.HazeState()
    this.background(GlassColors.Background)
        .hazeSource(hazeState)
}
