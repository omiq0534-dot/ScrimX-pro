import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace(
    "color = androidx.compose.ui.graphics.Color.White",
    "color = androidx.compose.ui.graphics.Color.Transparent"
)

content = content.replace(
    "modifier = Modifier.fillMaxSize(),",
    "modifier = Modifier.fillMaxSize().com.example.ui.theme.glassBackground(),"
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
