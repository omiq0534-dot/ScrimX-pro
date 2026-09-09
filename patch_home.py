import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")

# Fix main surface color
old_main = """    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF000000)
    ) {"""
new_main = """    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.ScreenBackground
    ) {"""
content = content.replace(old_main, new_main)

# App Bar Top Box
content = content.replace(
    'Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF090B10)).padding(horizontal = 16.dp, vertical = 14.dp))',
    'Box(modifier = Modifier.fillMaxWidth().background(AppColors.CardBackground).padding(horizontal = 16.dp, vertical = 14.dp))'
)
# Top Bar Text Colors
content = content.replace(
    'Text("SCRIMX", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White, letterSpacing = 1.sp)',
    'Text("SCRIMX", fontWeight = FontWeight.Black, fontSize = 22.sp, color = AppColors.TextPrimary, letterSpacing = 1.sp)'
)
content = content.replace(
    'Text("ESPORTS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFFFFD700), letterSpacing = 2.sp)',
    'Text("ESPORTS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = AppColors.PrimaryAccent, letterSpacing = 2.sp)'
)

# Notification icon tint
content = content.replace(
    'Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White, modifier = Modifier.size(22.dp))',
    'Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = AppColors.TextPrimary, modifier = Modifier.size(22.dp))'
)

# Game Category Headers
content = content.replace(
    'Text("BATTLEGROUNDS MOBILE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)',
    'Text("BATTLEGROUNDS MOBILE", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)'
)
content = content.replace(
    'Text("FREE FIRE MAX", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)',
    'Text("FREE FIRE MAX", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

print("HomeScreen root backgrounds patched.")
