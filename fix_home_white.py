import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("contentColor = Color.White", "contentColor = AppColors.TextPrimary")
content = content.replace("color = if (d % 2 == 0) Color.White else Color(0xFFFFD700)", "color = if (d % 2 == 0) AppColors.TextPrimary else Color(0xFFFFD700)")
content = content.replace("containerColor = if (spinsRemaining > 0) Color.White else Color(0xFF374151)", "containerColor = if (spinsRemaining > 0) AppColors.ButtonContainer else AppColors.BorderColor")
content = content.replace('Icon(Icons.Default.Star, contentDescription = "Coins", tint = Color.White,', 'Icon(Icons.Default.Star, contentDescription = "Coins", tint = AppColors.TextPrimary,')
content = content.replace('tint = Color.White, modifier = Modifier.size(28.dp)', 'tint = AppColors.TextPrimary, modifier = Modifier.size(28.dp)')
content = content.replace('.background(Color.White.copy(alpha = 0.08f))', '.background(AppColors.TextPrimary.copy(alpha = 0.08f))')

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
