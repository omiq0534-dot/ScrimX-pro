import re

auth_screens = ["app/src/main/java/com/example/ui/screens/LoginScreen.kt", "app/src/main/java/com/example/ui/screens/SplashScreen.kt", "app/src/main/java/com/example/ui/screens/CustomerSupportScreen.kt"]

import_colors = "import com.example.ui.theme.AppColors\n"

for file_path in auth_screens:
    try:
        with open(file_path, "r") as f:
            content = f.read()

        if "import com.example.ui.theme.AppColors" not in content:
            content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")
            
        content = content.replace('Color.Black', 'AppColors.ScreenBackground')
        content = content.replace('Color(0xFF000000)', 'AppColors.ScreenBackground')
        content = content.replace('Color(0xFF0D0D11)', 'AppColors.ScreenBackground')
        content = content.replace('Color(0xFF0B0F14)', 'AppColors.ScreenBackground')
        
        content = content.replace('Color.White', 'AppColors.TextPrimary')
        content = content.replace('Color(0xFF888888)', 'AppColors.TextSecondary')
        content = content.replace('Color(0xFFA0AEC0)', 'AppColors.TextSecondary')
        content = content.replace('Color(0xFF757A8C)', 'AppColors.TextSecondary')
        
        content = content.replace('Color(0xFF13141B)', 'AppColors.CardBackground')
        content = content.replace('Color(0xFF131316)', 'AppColors.CardBackground')
        content = content.replace('Color(0xFF1A1A22)', 'AppColors.CardBackground')
        
        content = content.replace('Color(0xFF26262D)', 'AppColors.BorderColor')
        content = content.replace('Color(0xFF333333)', 'AppColors.BorderColor')
        content = content.replace('Color(0xFF1E293B)', 'AppColors.BorderColor')

        with open(file_path, "w") as f:
            f.write(content)
        print(f"{file_path} patched")
    except Exception as e:
        print(f"Skipped {file_path}")
