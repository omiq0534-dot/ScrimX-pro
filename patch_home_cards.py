import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Update Box Background colors
content = content.replace('.background(Color(0xFF0F172A))', '.background(AppColors.CardBackground)')
content = content.replace('.background(Color(0xFF1E293B))', '.background(AppColors.SubCardBackground)')
content = content.replace('.background(Color(0xFF0A0F1C))', '.background(AppColors.CardBackground)')
content = content.replace('.background(Color(0xFF162032))', '.background(AppColors.SubCardBackground)')
content = content.replace('.border(1.dp, Color(0xFF1E293B)', '.border(1.dp, AppColors.BorderColor')
content = content.replace('.border(1.dp, Color(0xFF334155)', '.border(1.dp, AppColors.BorderColor')

# Text Color
content = content.replace('color = Color.White', 'color = AppColors.TextPrimary')
content = content.replace('color = Color(0xFFE2E8F0)', 'color = AppColors.TextPrimary')
content = content.replace('color = Color(0xFF94A3B8)', 'color = AppColors.TextSecondary')
content = content.replace('color = Color(0xFFA0AEC0)', 'color = AppColors.TextSecondary')

# SubCard border
content = content.replace('Color(0xFF2E3B4E)', 'AppColors.BorderColor')

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

print("HomeScreen components patched.")
