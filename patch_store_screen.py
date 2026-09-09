import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")

# Backgrounds
content = content.replace('containerColor = Color.Black', 'containerColor = AppColors.ScreenBackground')
content = content.replace('background(Color(0xFF000000))', 'background(AppColors.ScreenBackground)')
content = content.replace('containerColor = Color(0xFF0D0D11)', 'containerColor = AppColors.ScreenBackground')
content = content.replace('background(Color.Black)', 'background(AppColors.ScreenBackground)')

# Top bar texts
content = content.replace('Text("Store", fontWeight = FontWeight.Black, color = Color.White', 'Text("Store", fontWeight = FontWeight.Black, color = AppColors.TextPrimary')
content = content.replace('Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)', 'Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)')

# Tabs background
content = content.replace('.background(Color(0xFF131318))', '.background(AppColors.CardBackground)')

# Tab Item Text Colors
# This one might be complex. Let's just do targeted replacements.
content = content.replace('Color.White', 'AppColors.TextPrimary')
content = content.replace('Color(0xFF757A8C)', 'AppColors.TextSecondary')
content = content.replace('Color(0xFF131318)', 'AppColors.CardBackground')
content = content.replace('Color(0xFF26262D)', 'AppColors.BorderColor')
content = content.replace('Color(0xFF1B1B22)', 'AppColors.SubCardBackground')

# Undo if any specific ones got messed up
# PrimaryAccentText
content = content.replace('color = AppColors.TextPrimary // Black text on accent?', 'color = AppColors.PrimaryAccentText')

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)

print("StoreScreen patched.")
