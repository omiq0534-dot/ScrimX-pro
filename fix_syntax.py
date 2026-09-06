with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

bad_str = ".background(Color.Transparent) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFF1E2338), RoundedCornerShape(22.dp))\n            .padding(12.dp)"

good_str = ".background(Color.Transparent)\n            .padding(12.dp)"

content = content.replace(bad_str, good_str)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
