import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# Restore line 195
content = content.replace("containerColor = if (isItemActive) Color(0xFFFFD700) else borderColor", "containerColor = if (isItemActive) Color(0xFFFFD700) else AppColors.BorderColor")

# Replace line 1171 which is inside the linearGradient in TournamentDiscountCard
# The pattern should be `Color(0xFF1F1F1F),\n                            AppColors.BorderColor,`
content = content.replace("Color(0xFF1F1F1F),\n                            AppColors.BorderColor,", "Color(0xFF1F1F1F),\n                            borderColor,")

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)
