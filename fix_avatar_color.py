with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'text = initial,\n                color = Color.Black',
    'text = initial,\n                color = Color.White'
)

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)
