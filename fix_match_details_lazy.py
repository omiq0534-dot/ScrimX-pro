import re

with open('app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt', 'r') as f:
    content = f.read()

# Fix LazyColumn by reverting to Column and using verticalScroll
old_lazy_col = """        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0B0F14)),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {"""

new_col = """        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0B0F14))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {"""
content = content.replace(old_lazy_col, new_col)

# Fix imports again correctly!
content = content.replace("import androidx.compose.animation.core.*", "import androidx.compose.animation.core.*\nimport androidx.compose.animation.core.animateFloatAsState\nimport androidx.compose.animation.core.spring\nimport androidx.compose.foundation.interaction.MutableInteractionSource\nimport androidx.compose.foundation.interaction.collectIsPressedAsState\nimport androidx.compose.ui.draw.scale\nimport androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items")

with open('app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt', 'w') as f:
    f.write(content)
