import re

with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "r") as f:
    content = f.read()

# Remove glassCard and glassBackground from AppColors
content = re.sub(r'// 3D Glassmorphism Modifier.*?fun Modifier\.glassCard[\s\S]*?fun Modifier\.glassBackground.*$', '', content, flags=re.MULTILINE)

with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
    f.write(content)
