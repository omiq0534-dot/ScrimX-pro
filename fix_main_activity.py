with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("GlassScaffold { hazeState ->", "Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {")
content = content.replace("import com.example.ui.theme.GlassScaffold", "")
content = content.replace("import com.example.ui.theme.glassBackground", "")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
