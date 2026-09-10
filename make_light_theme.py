import os
import re

# 1. Update AppColors.kt to be fully Light Theme
app_colors = """package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val isDark: Boolean = false
    val ScreenBackground: Color = Color(0xFFF3F4F6) // Light Gray Background
    val TextPrimary: Color = Color(0xFF111827) // Dark text for light theme
    val TextOnLight: Color = Color(0xFF1F2937) 
    val TextSecondary: Color = Color(0xFF6B7280)
    val PrimaryAccent: Color = Color(0xFFEAB308) // Yellow Accent
    val PrimaryAccentText: Color = Color.White
    val Positive: Color = Color(0xFF10B981)
    val Negative: Color = Color(0xFFEF4444)
    val Info: Color = Color(0xFF3B82F6)
    val Divider: Color = Color(0xFFE5E7EB)
    val ButtonContainer: Color = Color(0xFFFACC15)
    val ButtonContent: Color = Color.Black
    val CardBackground: Color = Color.White // White Cards
    val SubCardBackground: Color = Color(0xFFF9FAFB) // Slightly off-white
    val BorderColor: Color = Color(0xFFE5E7EB)
}
"""
with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
    f.write(app_colors)

def replace_in_file(filepath, old, new):
    if not os.path.exists(filepath): return
    with open(filepath, "r") as f:
        content = f.read()
    content = content.replace(old, new)
    with open(filepath, "w") as f:
        f.write(content)

# Fix Login Screen Hardcoded Colors
replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "Color(0xFF111319)", "Color.White")
replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "Color(0xFF262A38)", "AppColors.BorderColor")
replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "Color(0xFF1E212D)", "Color(0xFFF3F4F6)")
replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "Color(0xFF2D3244)", "AppColors.BorderColor")
replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "Color(0xFF1A1D27)", "Color(0xFFF9FAFB)")
replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "Color(0xFF2E3346)", "AppColors.BorderColor")
replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "color = AppColors.ScreenBackground", "color = Color.White")

# Fix Match Details Hardcoded Colors
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF14151B)", "Color.White")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF0C0D11)", "Color(0xFFF9FAFB)")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF2E313D)", "AppColors.BorderColor")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF1F222C)", "Color(0xFFF3F4F6)")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFFAAAAAA)", "Color(0xFF4B5563)")

# Slot Card Colors
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF131922)", "Color.White")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF08080A).copy(alpha = 0.5f)", "Color(0xFFF3F4F6)")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF1E2430)", "AppColors.BorderColor")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF262A38)", "AppColors.BorderColor")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF08080A)", "Color(0xFFF9FAFB)")

# H2H Colors
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF1C1F2B)", "Color(0xFFF3F4F6)")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF161922)", "Color.White")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFF2E3346)", "AppColors.BorderColor")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "Color(0xFFD1D5DB)", "AppColors.BorderColor")

print("Theme changed to Light")
