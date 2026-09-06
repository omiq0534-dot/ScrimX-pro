with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('color = Color(0xFFFFD700),\n                            fontWeight = FontWeight.Black,\n                            fontSize = 13.sp', 'color = Color.Black,\n                            fontWeight = FontWeight.Black,\n                            fontSize = 13.sp')
    
with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
