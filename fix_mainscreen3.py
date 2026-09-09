with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# I want to remove the very last '}' 
last_brace_index = content.rfind("}")
if last_brace_index != -1:
    content = content[:last_brace_index] + content[last_brace_index+1:]

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
