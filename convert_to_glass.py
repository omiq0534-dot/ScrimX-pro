import os
import glob
import re

directories = [
    "app/src/main/java/com/example/ui/screens/*.kt",
    "app/src/main/java/com/example/ui/components/*.kt"
]

files_to_process = []
for d in directories:
    files_to_process.extend(glob.glob(d))

for file_path in files_to_process:
    with open(file_path, "r") as f:
        content = f.read()
    
    original = content
    
    # We want to replace sequences like:
    # .background(AppColors.CardBackground)
    # .border(1.dp, AppColors.BorderColor, RoundedCornerShape(24.dp))
    # with .glassCard(cornerRadius = 24.dp)
    
    # Or just simply replace background with glassCard, and remove the subsequent border if it exists.
    
    # A safer approach for now: replace `.background(AppColors.CardBackground)` with `.glassCard()`.
    # Replace `.background(AppColors.SubCardBackground)` with `.glassCard(alpha = 0.5f)`.
    # And then we can try to strip out `.border(...)` immediately following it if it matches.

    # First, let's just do simple replacements for testing
    content = re.sub(r'\.background\(\s*AppColors\.CardBackground\s*\)', '.glassCard()', content)
    content = re.sub(r'\.background\(\s*AppColors\.SubCardBackground\s*\)', '.glassCard(alpha = 0.5f)', content)

    # Let's also do `.background(Color(0xFF131316))` which is the old hardcoded card background
    content = re.sub(r'\.background\(\s*Color\(0xFF131316\)\s*\)', '.glassCard()', content)
    content = re.sub(r'\.background\(\s*Color\(0xFF1B1B1F\)\s*\)', '.glassCard(alpha = 0.5f)', content)
    
    # If the user also wants the background to be glassy, let's inject `glassBackground()` on the main screens.
    # HomeScreen: `.background(AppColors.ScreenBackground)` -> `.glassBackground()`
    content = re.sub(r'\.background\(\s*AppColors\.ScreenBackground\s*\)', '.glassBackground()', content)
    content = re.sub(r'\.background\(\s*Color\(0xFF000000\)\s*\)', '.glassBackground()', content)

    # Make sure we add import com.example.ui.theme.glassCard and glassBackground
    if ".glassCard" in content or ".glassBackground" in content:
        if "import com.example.ui.theme.glassCard" not in content:
            content = content.replace("import com.example.ui.theme.AppColors", "import com.example.ui.theme.AppColors\nimport com.example.ui.theme.glassCard\nimport com.example.ui.theme.glassBackground")
    
    if content != original:
        with open(file_path, "w") as f:
            f.write(content)

