import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace('containerColor = Color.Black', 'containerColor = AppColors.ScreenBackground')
content = content.replace('containerColor = Color(0xFF0D0D11)', 'containerColor = AppColors.ScreenBackground')
content = content.replace('containerColor = Color(0xFF000000)', 'containerColor = AppColors.ScreenBackground')
content = content.replace('containerColor = Color(0xFF0A0D14)', 'containerColor = AppColors.ScreenBackground')

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
