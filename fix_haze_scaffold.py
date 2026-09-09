import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Make sure we import GlassScaffold
if "import com.example.ui.theme.GlassScaffold" not in content:
    content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\nimport com.example.ui.theme.GlassScaffold")

# Change Scaffold to GlassScaffold in MainScreen
content = content.replace("Scaffold(", "GlassScaffold { hazeState ->\n    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent,")
content = content.replace("} // End Scaffold", "}\n    } // End GlassScaffold")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
