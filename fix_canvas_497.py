import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

old = "Canvas(\n                            modifier = Modifier.size(200.dp)\n                        ) {"
new = "val textPrimaryColor = AppColors.TextPrimary\n                        Canvas(\n                            modifier = Modifier.size(200.dp)\n                        ) {"
content = content.replace(old, new)

# And remove the `val textPrimaryColor = AppColors.TextPrimary` that was accidentally injected wrong
wrong_old = "val textPrimaryColor = AppColors.TextPrimary\n                    Canvas(modifier = Modifier\n                        .size(340.dp)"
wrong_new = "Canvas(modifier = Modifier\n                        .size(340.dp)"
content = content.replace(wrong_old, wrong_new)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
