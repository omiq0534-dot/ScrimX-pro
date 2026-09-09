import glob

files_to_process = glob.glob("app/src/main/java/com/example/ui/screens/*.kt") + glob.glob("app/src/main/java/com/example/ui/components/*.kt")

for file_path in files_to_process:
    with open(file_path, "r") as f:
        content = f.read()
    
    if "AppColors." in content and "import com.example.ui.theme.AppColors" not in content:
        content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\nimport com.example.ui.theme.AppColors")
        content = content.replace("package com.example.ui.components", "package com.example.ui.components\nimport com.example.ui.theme.AppColors")
        with open(file_path, "w") as f:
            f.write(content)

