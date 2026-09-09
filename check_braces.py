def check_braces(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    count = 0
    for char in content:
        if char == '{': count += 1
        elif char == '}': count -= 1
    print(f"{filepath}: {count}")

check_braces("app/src/main/java/com/example/ui/screens/MainScreen.kt")
