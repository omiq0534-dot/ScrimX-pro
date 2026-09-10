import os
import re

# 1. Fix AppColors.kt
app_colors = """package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val isDark: Boolean = true
    val ScreenBackground: Color = Color(0xFFFAFAFA) // Clean White Background
    val TextPrimary: Color = Color.White // Text inside dark cards (Default)
    val TextOnLight: Color = Color(0xFF1E293B) // Text on white background
    val TextSecondary: Color = Color(0xFF94A3B8)
    val PrimaryAccent: Color = Color(0xFFFFD700)
    val PrimaryAccentText: Color = Color.Black
    val Positive: Color = Color(0xFF10B981)
    val Negative: Color = Color(0xFFEF4444)
    val Info: Color = Color(0xFF3B82F6)
    val Divider: Color = Color(0xFF1E2130)
    val ButtonContainer: Color = Color.White
    val ButtonContent: Color = Color.Black
    val CardBackground: Color = Color(0xFF14161F) // Dark Cards
    val SubCardBackground: Color = Color(0xFF1E2130) // Dark Inner Cards
    val BorderColor: Color = Color(0xFF2A2D3E)
}
"""
with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
    f.write(app_colors)

print("Fixed AppColors")

def replace_in_file(filepath, old, new):
    if not os.path.exists(filepath): return
    with open(filepath, "r") as f:
        content = f.read()
    content = content.replace(old, new)
    with open(filepath, "w") as f:
        f.write(content)

replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt",
                "containerColor = AppColors.TextPrimary,\n                        contentColor = AppColors.ScreenBackground",
                "containerColor = Color.White,\n                        contentColor = Color.Black")

replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt",
                ".background(AppColors.TextPrimary)",
                ".background(Color.White)")

print("Fixed LoginScreen")
