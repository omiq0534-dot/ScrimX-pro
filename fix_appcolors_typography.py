with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "r") as f:
    content = f.read()

content = content.replace("Color(0xFF000000)", "Color(0xFF1E293B)")
content = content.replace("Color(0xFF4B5563)", "Color(0xFF64748B)")
content = content.replace("Color(0xFFFFD700)", "Color(0xFFF59E0B)")
content = content.replace("Color(0xFF000000)", "Color(0xFF1E293B)")

# Ensure TextPrimary is updated everywhere
content = content.replace("Color.Black", "Color(0xFF1E293B)")

with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
    f.write(content)
