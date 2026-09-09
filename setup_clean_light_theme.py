import re
import os

# 1. Setup AppColors.kt for a clean Light Theme
app_colors_content = """package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val isDark: Boolean = false
    val ScreenBackground: Color = Color(0xFFFAFAFA)
    val TextPrimary: Color = Color(0xFF1E293B)
    val TextSecondary: Color = Color(0xFF64748B)
    val TextHighlight: Color = Color(0xFF1E293B)
    val PrimaryAccent: Color = Color.White
    val PrimaryAccentText: Color = Color.Black
    val Positive: Color = Color(0xFF10B981)
    val Negative: Color = Color(0xFFEF4444)
    val Info: Color = Color(0xFF3B82F6)
    val Divider: Color = Color(0xFFE2E8F0)
    val ButtonContainer: Color = Color.White
    val ButtonContent: Color = Color.Black
    val CardBackground: Color = Color.White
    val SubCardBackground: Color = Color(0xFFF1F5F9)
    val BorderColor: Color = Color(0xFFE2E8F0)
}
"""
with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
    f.write(app_colors_content)

# 2. Fix ProfileScreen.kt
with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

# Fix StatItem text color to White (since it's inside a dark card)
content = re.sub(
    r'fun StatItem\(title: String, value: String\)\s*\{\s*Column\(horizontalAlignment = Alignment\.CenterHorizontally\)\s*\{\s*Text\(value,\s*fontWeight = FontWeight\.Black,\s*fontSize = 22\.sp,\s*color = Color\.Black\)',
    'fun StatItem(title: String, value: String) {\\n    Column(horizontalAlignment = Alignment.CenterHorizontally) {\\n        Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)',
    content
)

# Fix Win Rate and Avg K/D in ProfileScreen to White
content = content.replace('Text(winRate, color = Color.Black', 'Text(winRate, color = Color.White')
content = content.replace('Text(kdRatio, color = Color.Black', 'Text(kdRatio, color = Color.White')

# Fix Settings menu container background to AppColors.CardBackground instead of hardcoded White
content = content.replace('.background(Color.White)', '.background(AppColors.CardBackground)')
# Fix Settings menu border to AppColors.BorderColor
content = content.replace('.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))', '.border(1.dp, AppColors.BorderColor, RoundedCornerShape(24.dp))')
# SettingsRow icon color to AppColors.TextPrimary
content = content.replace('tint = if (isDestructive) Color(0xFFF44336) else Color.Black', 'tint = if (isDestructive) Color(0xFFF44336) else AppColors.TextPrimary')
# SettingsRow title color to AppColors.TextPrimary
content = content.replace('color = if (isDestructive) Color(0xFFF44336) else Color.Black', 'color = if (isDestructive) Color(0xFFF44336) else AppColors.TextPrimary')

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)

print("Done")
