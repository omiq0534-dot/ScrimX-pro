with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.composed\nimport androidx.compose.foundation.layout.fillMaxSize\n", "")
content = content.replace("package com.example.ui.theme", "package com.example.ui.theme\nimport androidx.compose.ui.composed\nimport androidx.compose.foundation.layout.fillMaxSize")

with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "w") as f:
    f.write(content)
