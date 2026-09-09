with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

imports_to_add = """
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.BlendMode
"""

if "import androidx.compose.animation.AnimatedContent" not in content:
    content = content.replace("import androidx.compose.animation.fadeOut", "import androidx.compose.animation.fadeOut" + imports_to_add)

content = content.replace("DataRechargePlanCard(", "DataRechargePlanCard(plan = plan, ")

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)
