import re
import os

# 1. Restore AppColors to the original MIXED state
app_colors_content = """package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    val isDark: Boolean = true
    val ScreenBackground: Color = Color(0xFFFAFAFA) // Clean White Background
    val TextPrimary: Color = Color.White // Text inside dark cards
    val TextSecondary: Color = Color(0xFF94A3B8)
    val TextHighlight: Color = Color.White
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
    f.write(app_colors_content)


# 2. Fix ProfileScreen
with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    profile = f.read()

# The Settings List Container should be White
profile = profile.replace('.background(AppColors.CardBackground)', '.background(Color.White)')
profile = profile.replace('.border(1.dp, AppColors.BorderColor, RoundedCornerShape(24.dp))', '.border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(24.dp))')

# SettingsRow colors
# We need to make sure the icon background is light gray
profile = profile.replace('.background(if (isDestructive) Color(0xFFFFEBEE) else AppColors.SubCardBackground)', '.background(if (isDestructive) Color(0xFFFFEBEE) else Color(0xFFF3F4F6))')
# Icon tint
profile = profile.replace('tint = if (isDestructive) Color(0xFFF44336) else AppColors.TextPrimary', 'tint = if (isDestructive) Color(0xFFF44336) else Color(0xFF1E293B)')
# Text color
profile = profile.replace('color = if (isDestructive) Color(0xFFF44336) else AppColors.TextPrimary', 'color = if (isDestructive) Color(0xFFF44336) else Color(0xFF1E293B)')

# "ROAD TO PRO [X]" text should be white because it's inside a dark card
profile = profile.replace('Text(\n                            "ROAD TO PRO [X]",\n                            color = Color.Black', 'Text(\n                            "ROAD TO PRO [X]",\n                            color = Color.White')
# Same for any other black text inside the dark cards
profile = profile.replace('Text(winRate, color = Color.Black', 'Text(winRate, color = Color.White')
profile = profile.replace('Text(kdRatio, color = Color.Black', 'Text(kdRatio, color = Color.White')

# Also fix the StatItem
profile = re.sub(
    r'fun StatItem\(title: String, value: String\)\s*\{\s*Column\(horizontalAlignment = Alignment\.CenterHorizontally\)\s*\{\s*Text\(value,\s*fontWeight = FontWeight\.Black,\s*fontSize = 22\.sp,\s*color = Color\.Black\)',
    'fun StatItem(title: String, value: String) {\\n    Column(horizontalAlignment = Alignment.CenterHorizontally) {\\n        Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)',
    profile
)

# Profile Name should be Black because it's on the white background
profile = profile.replace('text = name, \n                    fontSize = 20.sp, \n                    fontWeight = FontWeight.Black, \n                    color = Color.White', 'text = name, \n                    fontSize = 20.sp, \n                    fontWeight = FontWeight.Black, \n                    color = Color.Black')
profile = profile.replace('text = name,\n                    fontSize = 20.sp,\n                    fontWeight = FontWeight.Black,\n                    color = Color.White', 'text = name,\n                    fontSize = 20.sp,\n                    fontWeight = FontWeight.Black,\n                    color = Color.Black')

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(profile)


# 3. Fix HomeScreen
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    home = f.read()

# Ensure section titles are Black
home = home.replace('Text("Earning Zone", fontSize = 22.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary)', 'Text("Earning Zone", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)')
home = home.replace('Text("Upcoming Scrims", fontSize = 22.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary)', 'Text("Upcoming Scrims", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)')
home = home.replace('Text("See All", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)', 'Text("See All", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)')

# Ensure EarnCard title is white (TextPrimary)
home = home.replace('Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.Black', 'Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = AppColors.TextPrimary')

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(home)

print("Done")
