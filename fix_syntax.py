import glob
import re

files_to_process = glob.glob("app/src/main/java/com/example/ui/screens/*.kt") + glob.glob("app/src/main/java/com/example/ui/components/*.kt") + ["app/src/main/java/com/example/MainActivity.kt"]

for file_path in files_to_process:
    with open(file_path, "r") as f:
        content = f.read()
    
    original = content
    
    # Fix MainActivity
    content = content.replace("modifier = Modifier.fillMaxSize().com.example.ui.theme.glassBackground(),", "modifier = Modifier.fillMaxSize().glassBackground(),")
    if "glassBackground" in content and "import com.example.ui.theme.glassBackground" not in content:
        content = content.replace("import com.example.ui.theme.MyApplicationTheme", "import com.example.ui.theme.MyApplicationTheme\nimport com.example.ui.theme.glassBackground")

    # Remove rogue parenthesis after glassCard()
    # It looks like:
    # .glassCard()
    # )
    # .padding
    
    content = re.sub(r'\.glassCard\([^)]*\)\s*\)', r'.glassCard()', content)
    # the above might match correctly if there are no arguments. If there are arguments like alpha = 0.5f:
    content = re.sub(r'(\.glassCard\([^)]*\))\s*\)', r'\1', content)

    if content != original:
        with open(file_path, "w") as f:
            f.write(content)
