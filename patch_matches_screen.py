import re

with open("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")

# Replace Backgrounds
content = content.replace('Color.Black', 'AppColors.ScreenBackground')
content = content.replace('Color(0xFF0D0D11)', 'AppColors.ScreenBackground')
content = content.replace('Color(0xFF000000)', 'AppColors.ScreenBackground')

# Replace white text with dynamic
content = content.replace('Color.White', 'AppColors.TextPrimary')
content = content.replace('Color(0xFFAAAAAA)', 'AppColors.TextSecondary')
content = content.replace('Color(0xFF8B8F9C)', 'AppColors.TextSecondary')
content = content.replace('Color(0xFF757A8C)', 'AppColors.TextSecondary')

# Replace Cards
content = content.replace('Color(0xFF13141B)', 'AppColors.CardBackground')
content = content.replace('Color(0xFF131316)', 'AppColors.CardBackground')
content = content.replace('Color(0xFF1B1B1F)', 'AppColors.SubCardBackground')

# Replace Borders
content = content.replace('Color(0xFF26262D)', 'AppColors.BorderColor')
content = content.replace('Color(0xFF232533)', 'AppColors.BorderColor')
content = content.replace('Color(0xFF1E293B)', 'AppColors.BorderColor')

with open("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "w") as f:
    f.write(content)
print("MatchesScreen patched")
