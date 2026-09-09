with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Make sure HomeScreen uses AppColors.ScreenBackground
content = content.replace('.background(Color(0xFFFAFAFA))', '.background(AppColors.ScreenBackground)')
content = content.replace('.background(Color(0xFFF9FAFB))', '.background(AppColors.ScreenBackground)')
content = content.replace('.background(Color(0xFF0A0B10))', '.background(AppColors.ScreenBackground)')

# If EarnCard title text is hardcoded to black, use AppColors.TextPrimary
content = content.replace('Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.Black', 'Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = AppColors.TextPrimary')

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
print("Done")
