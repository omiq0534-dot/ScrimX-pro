with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.navigation.compose.currentBackStackEntryAsState()", "navController.currentBackStackEntryAsState()")
content = content.replace("androidx.compose.ui.graphics.drawscope.rotate(angle)", "rotate(angle)")

if "import androidx.compose.ui.graphics.drawscope.rotate" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.drawscope.rotate\nimport androidx.compose.ui.graphics.Color")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
