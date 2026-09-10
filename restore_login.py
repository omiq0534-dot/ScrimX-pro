import os

def replace_in_file(filepath, old, new):
    if not os.path.exists(filepath): return
    with open(filepath, "r") as f:
        content = f.read()
    content = content.replace(old, new)
    with open(filepath, "w") as f:
        f.write(content)

replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt",
                "containerColor = Color.White,\n                        contentColor = Color.Black",
                "containerColor = AppColors.TextPrimary,\n                        contentColor = AppColors.ScreenBackground")

replace_in_file("app/src/main/java/com/example/ui/screens/LoginScreen.kt",
                ".background(Color.White)",
                ".background(AppColors.ScreenBackground)")

print("Restored LoginScreen")
