import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# Replace hardcoded dark colors that will clash with the light theme
content = content.replace("containerColor = AppColors.TextPrimary", "containerColor = androidx.compose.ui.graphics.Color.White")
content = content.replace("Color(0xFF0A0B10)", "androidx.compose.ui.graphics.Color(0xFFF1F5F9)")
content = content.replace("Color(0xFF262938)", "androidx.compose.ui.graphics.Color(0xFFE2E8F0)")
content = content.replace("Color(0xFF0F1118)", "androidx.compose.ui.graphics.Color.White")
content = content.replace("Color(0xFF181A25)", "androidx.compose.ui.graphics.Color(0xFFF8FAFC)")
content = content.replace("Color.White", "androidx.compose.ui.graphics.Color.Black")

# Wait, replacing all Color.White to Color.Black might break some things, 
# but since it's a light theme, most White text should be Black, and most White backgrounds should be glass or white.
# Actually let's just replace specific ones.
