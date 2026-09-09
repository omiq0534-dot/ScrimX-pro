import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# Fix the two lines
content = content.replace("color = AppColors.TextPrimary.copy(alpha = 0.5f),", "color = textPrimaryColor.copy(alpha = 0.5f),")
content = content.replace("color = AppColors.TextPrimary.copy(alpha = 0.2f),", "color = textPrimaryColor.copy(alpha = 0.2f),")

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)
