#!/bin/bash

# Add imports
sed -i '1s/^/import androidx.compose.animation.core.*\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.ui.draw.drawBehind\nimport androidx.compose.ui.graphics.Brush\nimport androidx.compose.ui.graphics.drawscope.rotate\n/' app/src/main/java/com/example/ui/screens/MainScreen.kt
