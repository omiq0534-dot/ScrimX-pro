with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Color(0xFF1E2338) else if (isDataRecharge)', 'Color.Gray else if (isDataRecharge)')

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
