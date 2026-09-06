with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    lines = f.readlines()
for line in lines:
    if 'import ' in line:
        pass
