import glob
import re

files_to_process = glob.glob("app/src/main/java/com/example/ui/screens/*.kt")

for file_path in files_to_process:
    with open(file_path, "r") as f:
        content = f.read()
    
    original = content
    
    content = content.replace(
        "containerColor = AppColors.ScreenBackground",
        "containerColor = androidx.compose.ui.graphics.Color.Transparent"
    )
    
    if content != original:
        with open(file_path, "w") as f:
            f.write(content)
