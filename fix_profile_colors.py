with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace('Color(0xFFE5E7EB)', 'AppColors.BorderColor')
content = content.replace('Color(0xFFF3F4F6)', 'AppColors.Divider')
content = content.replace('Color(0xFFF9FAFB)', 'AppColors.ScreenBackground') # If there's any off-white background

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)
