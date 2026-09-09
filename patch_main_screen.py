import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")

# Update Scaffold bottom bar colors
content = content.replace('containerColor = Color(0xFF0D0D11)', 'containerColor = AppColors.ScreenBackground')
content = content.replace('containerColor = Color.Black', 'containerColor = AppColors.ScreenBackground')
content = content.replace('containerColor = Color(0xFF0A0D14)', 'containerColor = AppColors.ScreenBackground')

# Nav bar styling
content = content.replace('.background(Color(0xFF090B10))', '.background(AppColors.CardBackground)')
content = content.replace('.border(1.dp, Color(0xFF1F2433)', '.border(1.dp, AppColors.BorderColor')
content = content.replace('.border(1.dp, Color(0xFF26262D)', '.border(1.dp, AppColors.BorderColor')

# Selected / Unselected item colors
# Wait, let's see how MainScreen sets up bottom nav.
