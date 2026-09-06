with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('color = if (isUsed) Color(0xFF6F8299) else Color(0xFF22C55E)', 'color = if (isUsed) Color.DarkGray else Color(0xFF22C55E)')

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

