import re

with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "r") as f:
    content = f.read()

# Make ScreenBackground a bit cooler/mesh-like color for light theme, so translucent white pops.
# Light mode ScreenBackground: A very light icy blue-gray
content = content.replace('Color(0xFFF8F9FA)', 'Color(0xFFF2F4F8)')
# CardBackground: Translucent white for glassmorphism
content = content.replace('Color(0xFFFFFFFF)', 'Color(0xCCFFFFFF)')

with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
    f.write(content)
