import re

with open("app/src/main/java/com/example/ui/components/PremiumMatchCard.kt", "r") as f:
    content = f.read()

# Replace hardcoded copy instances
content = content.replace("Color.White.copy", "AppColors.TextPrimary.copy")
# Replace button colors
content = content.replace("containerColor = if (isLive || isCompleted) Color(0xFF222226) else Color.White,", "containerColor = if (isLive || isCompleted) AppColors.SubCardBackground else AppColors.ButtonContainer,")
content = content.replace("contentColor = if (isLive || isCompleted) Color.White else Color.Black", "contentColor = if (isLive || isCompleted) AppColors.TextPrimary else AppColors.ButtonContent")
# Check if any hardcoded Color(0xFF... are still there
content = content.replace("Color(0xFF131316)", "AppColors.CardBackground")
content = content.replace("Color(0xFF1A1A1E)", "AppColors.SubCardBackground")
content = content.replace("Color(0xFF26262D)", "AppColors.BorderColor")
content = content.replace("Color(0xFFFFFFFF)", "AppColors.TextPrimary")

with open("app/src/main/java/com/example/ui/components/PremiumMatchCard.kt", "w") as f:
    f.write(content)

