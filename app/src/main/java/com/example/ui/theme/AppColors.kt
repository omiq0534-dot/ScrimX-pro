package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppColors {
    // FORCING LIGHT/GLASS MODE FOR TESTING
    val isDark: Boolean = false

    val ScreenBackground: Color = Color(0xFFF0F4F8)

    // FORCE ALL TEXT TO BLACK FOR GLASS EFFECT
    val TextPrimary: Color = Color(0xFF1E293B)
    val TextSecondary: Color = Color(0xFF64748B)
    val TextHighlight: Color = Color(0xFFD97706)

    val PrimaryAccent: Color = Color(0xFFF59E0B)
    val PrimaryAccentText: Color = Color(0xFF1E293B)
    val Positive: Color = Color(0xFF10B981)
    val Negative: Color = Color(0xFFEF4444)
    val Info: Color = Color(0xFF06B6D4)

    val Divider: Color = Color(0x33000000)
    val ButtonContainer: Color = Color(0x99FFFFFF)
    val ButtonContent: Color = Color(0xFF1E293B)

    val CardBackground: Color = Color(0x99FFFFFF)
    val SubCardBackground: Color = Color(0x66FFFFFF)
    val BorderColor: Color = Color(0x80FFFFFF)
}
