import re

with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "r") as f:
    content = f.read()

content = content.replace("package com.example.ui.theme", "package com.example.ui.theme\nimport dev.chrisbanes.haze.hazeSource")
content = content.replace("val LocalHazeState = androidx.compose.runtime.compositionLocalOf<HazeState> {\n    error(\"No HazeState provided\")\n}", "val LocalHazeState = androidx.compose.runtime.compositionLocalOf<HazeState?> { null }")
content = content.replace("val hazeState = try { LocalHazeState.current } catch (e: Exception) { remember { HazeState() } }", "val hazeState = LocalHazeState.current ?: remember { HazeState() }")
content = content.replace("val hazeState = LocalHazeState.current", "val hazeState = LocalHazeState.current ?: HazeState()")

# Also fix the call inside WhiteGlassCard
content = content.replace("val hazeState = LocalHazeState.current ?: HazeState()\n    WhiteGlassCard(", "val hazeState = LocalHazeState.current ?: remember { HazeState() }\n    WhiteGlassCard(")

with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "w") as f:
    f.write(content)
