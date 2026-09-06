import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Add blur import if missing
if "import androidx.compose.ui.draw.blur" not in content:
    content = content.replace("import androidx.compose.ui.draw.clip", "import androidx.compose.ui.draw.clip\nimport androidx.compose.ui.draw.blur")

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

