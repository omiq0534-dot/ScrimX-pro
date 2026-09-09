import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent,", "Scaffold(containerColor = AppColors.ScreenBackground,")
content = content.replace("import com.example.ui.theme.GlassScaffold", "")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
