import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Canvas(modifier = Modifier.fillMaxSize()) {", "val borderColor = AppColors.BorderColor\n            Canvas(modifier = Modifier.fillMaxSize()) {")

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)
