import glob
import re

files_to_process = glob.glob("app/src/main/java/com/example/ui/screens/*.kt") + glob.glob("app/src/main/java/com/example/ui/components/*.kt")

for file_path in files_to_process:
    with open(file_path, "r") as f:
        content = f.read()
    
    original = content
    
    # We want to remove lines containing `.border(` that appear within 2 lines after `.glassCard`
    # Let's just remove specific known hardcoded borders that might overlap.
    # Pattern: \.glassCard(.*?)\s*\.border\(1\.dp, AppColors\.BorderColor.*?\)
    # or just remove `AppColors.BorderColor` borders where it makes sense.
    
    content = re.sub(r'(\.glassCard\([^)]*\)\s*)\.border\([^)]*AppColors\.BorderColor[^)]*\)', r'\1', content)
    
    # Also for .glassCard() -> .border(1.dp, Color(0xFF...))
    content = re.sub(r'(\.glassCard\([^)]*\)\s*)\.border\([^)]*Color\([^)]*\)[^)]*\)', r'\1', content)

    if content != original:
        with open(file_path, "w") as f:
            f.write(content)
