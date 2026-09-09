with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace("bottomBar = { AppBottomNav(bottomNavController) },\n        containerColor = Color(0xFFFAFAFA)", "bottomBar = { AppBottomNav(bottomNavController) }")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
