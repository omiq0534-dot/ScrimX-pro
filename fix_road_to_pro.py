with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'Text(\n                            "ROAD TO PRO [X]",\n                            color = Color.Black',
    'Text(\n                            "ROAD TO PRO [X]",\n                            color = Color.White'
)

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)
print("Done")
