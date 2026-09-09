import re

with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "r") as f:
    content = f.read()

content = content.replace("val LocalHazeState = androidx.compose.runtime.compositionLocalOf<HazeState> {\n    error(\"No HazeState provided\")\n}", "val LocalHazeState = androidx.compose.runtime.compositionLocalOf<HazeState?> { null }")
content = content.replace("val hazeState = LocalHazeState.current ?: HazeState() ?: remember { HazeState() }", "val hazeState = LocalHazeState.current ?: dev.chrisbanes.haze.HazeState()")

with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "w") as f:
    f.write(content)
