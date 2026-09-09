with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace('AppColors.BorderColor', 'Color(0xFFE5E7EB)')
content = content.replace('AppColors.Divider', 'Color(0xFFF3F4F6)')
content = content.replace('AppColors.ScreenBackground', 'Color(0xFFF9FAFB)') 

# But the menu container in ProfileScreen was: .background(AppColors.CardBackground)
content = content.replace('.background(AppColors.CardBackground)', '.background(Color.White)')

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)
