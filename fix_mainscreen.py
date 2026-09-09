with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Remove `GlassScaffold { hazeState ->`
content = content.replace("    GlassScaffold { hazeState ->\n    Scaffold(", "    Scaffold(")

# Remove the closing brace of GlassScaffold
# It's at the very end of the file
# Let's use regex or just rfind
last_brace_index = content.rfind("}")
if last_brace_index != -1:
    content = content[:last_brace_index] + content[last_brace_index+1:]
    last_brace_index2 = content.rfind("}")
    if last_brace_index2 != -1:
        content = content[:last_brace_index2] + content[last_brace_index2+1:]
        content = content + "}\n"

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
