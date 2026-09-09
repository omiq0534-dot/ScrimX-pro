import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Insert ThemeManager initialization
init_call = "com.example.ui.theme.ThemeManager.init(this)\n    enableEdgeToEdge()"
content = content.replace("enableEdgeToEdge()", init_call)

# Insert state observer
state_observer = """
    setContent {
      val isDarkTheme = androidx.compose.runtime.collectAsState(com.example.ui.theme.ThemeManager.isDarkTheme).value
      MyApplicationTheme(darkTheme = isDarkTheme, dynamicColor = false) {
"""
content = re.sub(r"setContent \{\s+MyApplicationTheme \{", state_observer, content)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

print("Patched MainActivity.kt")
