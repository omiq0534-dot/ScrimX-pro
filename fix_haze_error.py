with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "r") as f:
    content = f.read()

content = content.replace(
    'val LocalHazeState = androidx.compose.runtime.compositionLocalOf<HazeState> {     error("No HazeState provided") }',
    'val LocalHazeState = androidx.compose.runtime.compositionLocalOf<HazeState?> { null }'
)

with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "w") as f:
    f.write(content)
