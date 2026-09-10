import os

app_colors = """package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val isDark: Boolean = true
    val ScreenBackground: Color = Color(0xFF08080B) // Deep space black
    val TextPrimary: Color = Color(0xFFE2E8F0) // Clean off-white
    val TextSecondary: Color = Color(0xFF94A3B8)
    val PrimaryAccent: Color = Color(0xFFFFD700) // Electric Gold
    val PrimaryAccentText: Color = Color.Black
    val Positive: Color = Color(0xFF10B981) // Neon Green
    val Negative: Color = Color(0xFFEF4444) // Neon Red
    val Info: Color = Color(0xFF3B82F6) // Neon Blue
    val Divider: Color = Color(0xFF1E2130)
    val ButtonContainer: Color = Color(0xFFFACC15) // Deep yellow/gold
    val ButtonContent: Color = Color.Black
    val CardBackground: Color = Color(0xFF111319) // Very dark blue/black
    val SubCardBackground: Color = Color(0xFF1A1D27) // Slightly lighter
    val BorderColor: Color = Color(0xFF262A38) // Crisp subtle border
}
"""
with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
    f.write(app_colors)

print("Restored AppColors")
