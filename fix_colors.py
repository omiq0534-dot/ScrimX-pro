import glob
import os

for filepath in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filepath, "r") as f:
        content = f.read()

    # We do a line-by-line or selective replacement to avoid replacing the exceptions.
    lines = content.split("\n")
    new_lines = []
    
    for line in lines:
        # Keep placeholders grey
        if 'placeholder = { Text(' in line and 'color = Color.Gray' in line:
            new_lines.append(line)
        # Keep Bottom Nav unselected items grey
        elif 'unselectedIconColor = Color.Gray' in line or 'unselectedTextColor = Color.Gray' in line:
            new_lines.append(line)
        else:
            # Replace Color.Gray and Color.DarkGray with Color.Black
            modified_line = line.replace("Color.Gray", "Color.Black").replace("Color.DarkGray", "Color.Black")
            new_lines.append(modified_line)
            
    with open(filepath, "w") as f:
        f.write("\n".join(new_lines))

print("Colors updated successfully!")
