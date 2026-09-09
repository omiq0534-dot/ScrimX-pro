import glob

files_to_process = glob.glob("app/src/main/java/com/example/ui/screens/*.kt") + glob.glob("app/src/main/java/com/example/ui/components/*.kt")

for file_path in files_to_process:
    with open(file_path, "r") as f:
        content = f.read()
    
    if "AppColorsimport" in content:
        content = content.replace("AppColorsimport", "AppColors\nimport")
        with open(file_path, "w") as f:
            f.write(content)

