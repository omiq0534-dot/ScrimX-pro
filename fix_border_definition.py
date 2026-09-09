import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# Just put it at the beginning of the Canvas block
old_canvas = "Canvas(modifier = Modifier.matchParentSize()) {"
new_canvas = "val borderColor = AppColors.BorderColor\n        Canvas(modifier = Modifier.matchParentSize()) {"

content = content.replace("Color(0xFF1F1F1F),\n                            AppColors.BorderColor,", "Color(0xFF1F1F1F),\n                            borderColor,")
content = content.replace(old_canvas, new_canvas)

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)

