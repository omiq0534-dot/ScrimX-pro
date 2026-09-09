import re

with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "r") as f:
    content = f.read()

# find the end of object AppColors { ... }
# We can just match the object block
match = re.search(r'object AppColors \{.*?\n\}', content, re.DOTALL)
if match:
    new_content = content[:match.end()] + "\n"
    with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
        f.write(new_content)
