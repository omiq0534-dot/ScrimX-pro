import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# Replace all `AppColors.TextPrimary.copy` with `canvasColor.copy`
content = content.replace("AppColors.TextPrimary.copy", "canvasColor.copy")

# Now inject `val canvasColor = AppColors.TextPrimary` before every `Canvas` block that needs it.
# Actually, I can just find `Canvas(modifier =` or `Canvas(modifier = Modifier.matchParentSize()) {`
# Let's replace `Canvas(modifier = Modifier.matchParentSize()) {` with `val canvasColor = AppColors.TextPrimary\n        Canvas(modifier = Modifier.matchParentSize()) {`

# The exact indentation might vary. Let's just do:
content = content.replace(
    "Canvas(modifier = Modifier.matchParentSize()) {",
    "val canvasColor = AppColors.TextPrimary\n        Canvas(modifier = Modifier.matchParentSize()) {"
)

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)

