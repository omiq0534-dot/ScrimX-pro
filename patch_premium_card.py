import re

with open("app/src/main/java/com/example/ui/components/PremiumMatchCard.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")

# Update Box Container for Match Card
content = content.replace(
    '.background(Color(0xFF0D0D0F))',
    '.background(AppColors.CardBackground)'
)
content = content.replace(
    '.border(1.2.dp, Color(0xFF2A2A2E), RoundedCornerShape(20.dp))',
    '.border(1.2.dp, AppColors.BorderColor, RoundedCornerShape(20.dp))'
)

# Fix Title text color
content = content.replace(
    'text = title,\n                        color = Color.White',
    'text = title,\n                        color = AppColors.TextPrimary'
)

# Text inside Mode Badge
content = content.replace(
    'text = "$map • $badge",\n                        color = Color.White',
    'text = "$map • $badge",\n                        color = AppColors.TextPrimary'
)

# Fix Badge Background
content = content.replace(
    '.background(Color(0xFF1C1C20))',
    '.background(AppColors.SubCardBackground)'
)
content = content.replace(
    '.border(1.dp, Color(0xFF38383E), RoundedCornerShape(6.dp))',
    '.border(1.dp, AppColors.BorderColor, RoundedCornerShape(6.dp))'
)

# Fix CleanPrize / Entry text color
content = content.replace(
    'cleanPrize,\n                                color = Color.White',
    'cleanPrize,\n                                color = AppColors.TextPrimary'
)
content = content.replace(
    'cleanEntry,\n                                color = Color.White',
    'cleanEntry,\n                                color = AppColors.TextPrimary'
)

# Fix Bolt / Trophy icons
content = content.replace(
    'tint = Color.White, modifier = Modifier.size(15.dp)',
    'tint = AppColors.TextPrimary, modifier = Modifier.size(15.dp)'
)

# Fix Progress Bar Track
content = content.replace(
    'color = Color.White,\n                    trackColor = Color(0xFF26262B)',
    'color = AppColors.PrimaryAccent,\n                    trackColor = AppColors.SubCardBackground'
)

with open("app/src/main/java/com/example/ui/components/PremiumMatchCard.kt", "w") as f:
    f.write(content)

print("PremiumMatchCard dynamic colors patched.")
