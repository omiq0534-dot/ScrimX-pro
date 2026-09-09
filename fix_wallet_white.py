import re

with open("app/src/main/java/com/example/ui/screens/WalletScreen.kt", "r") as f:
    content = f.read()

content = content.replace("else Color.White", "else AppColors.TextPrimary")
content = content.replace(".background(Color.White)", ".background(AppColors.TextPrimary)")
content = content.replace('tint = Color.White, modifier = Modifier.size(14.dp)', 'tint = AppColors.TextPrimary, modifier = Modifier.size(14.dp)')
content = content.replace("Color.White.copy", "AppColors.TextPrimary.copy")
content = content.replace("containerColor = Color.White, contentColor = Color(0xFF111827)", "containerColor = AppColors.ButtonContainer, contentColor = AppColors.ButtonContent")
content = content.replace("containerColor = Color(0xFF272A3B), contentColor = Color.White", "containerColor = AppColors.CardBackground, contentColor = AppColors.TextPrimary")
content = content.replace("contentColor = Color.White),", "contentColor = AppColors.TextPrimary),")
content = content.replace("color = Color.White", "color = AppColors.TextPrimary")

with open("app/src/main/java/com/example/ui/screens/WalletScreen.kt", "w") as f:
    f.write(content)
