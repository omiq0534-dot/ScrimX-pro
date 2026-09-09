import re

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

content = content.replace(".background(Color.White)", ".background(AppColors.ScreenBackground)")
content = content.replace("CircularProgressIndicator(color = Color.White)", "CircularProgressIndicator(color = AppColors.TextPrimary)")
content = content.replace("CircularProgressIndicator(color = Color.White,", "CircularProgressIndicator(color = AppColors.TextPrimary,")
content = content.replace("Color.White", "AppColors.TextPrimary")

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)
