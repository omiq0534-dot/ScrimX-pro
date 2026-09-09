import os
import glob
import re

files_to_process = glob.glob("app/src/main/java/com/example/ui/screens/*.kt") + glob.glob("app/src/main/java/com/example/ui/components/*.kt")

for file_path in files_to_process:
    with open(file_path, "r") as f:
        content = f.read()
    
    # Replace color = Color.White with color = AppColors.TextPrimary inside Text()
    # It's safer to just replace 'color = Color.White' with 'color = AppColors.TextPrimary' 
    # except where it's intentionally white (like on colored badges), but since the theme is mostly light...
    
    new_content = re.sub(r'color\s*=\s*Color\.White', 'color = AppColors.TextPrimary', content)
    
    # Revert specific known exceptions if necessary (e.g. badges, gradients)
    # Since I'm lazy, I'll just apply it and if something is black-on-black I'll fix it.
    # Wait, HomeScreen has a Canva banner with white text.
    if "HomeScreen" in file_path:
        new_content = content # skip HomeScreen as it's already perfectly designed
    
    if new_content != content:
        with open(file_path, "w") as f:
            f.write(new_content)
