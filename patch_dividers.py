import re

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

# Replace those vertical dividers in StatsCard
content = content.replace(
    'HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.height(35.dp).width(1.dp))',
    'HorizontalDivider(color = AppColors.Divider, modifier = Modifier.height(35.dp).width(1.dp))'
)
# Make kd / winrate text colors dynamic
content = content.replace(
    'Text(winRate, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)',
    'Text(winRate, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black)'
)
content = content.replace(
    'Text(kdRatio, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)',
    'Text(kdRatio, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black)'
)

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)

print("Dividers patched.")
