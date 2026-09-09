import re
with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "r") as f:
    content = f.read()

content = re.sub(r'val LocalHazeState = androidx\.compose\.runtime\.compositionLocalOf<HazeState>\s*\{\s*error\("No HazeState provided"\)\s*\}', 
                 'val LocalHazeState = androidx.compose.runtime.compositionLocalOf<HazeState?> { null }', 
                 content)

# Also fix the composed block to properly remember!
content = re.sub(r'val hazeState = LocalHazeState\.current \?: dev\.chrisbanes\.haze\.HazeState\(\)',
                 'val hazeState = LocalHazeState.current ?: androidx.compose.runtime.remember { dev.chrisbanes.haze.HazeState() }',
                 content)

with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "w") as f:
    f.write(content)
