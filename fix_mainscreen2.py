with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Fix the broken end of file by appending the missing braces
# Since I removed two braces at the end, I need to add them back.
content = content + "\n    }\n}\n"

# Remove the actual GlassScaffold closing brace at line ~251
content = content.replace("    } // End GlassScaffold\n", "")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
