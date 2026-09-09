with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    c = f.read()
c = c.replace(
    "val isDarkTheme = androidx.compose.runtime.collectAsState(com.example.ui.theme.ThemeManager.isDarkTheme).value",
    "val isDarkTheme = com.example.ui.theme.ThemeManager.isDarkTheme.collectAsState().value"
)
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(c)
