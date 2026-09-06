import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# For the Price Breakdown Box, fix Color.White -> Color.Black
# Fix tint = Color.White
content = content.replace('tint = Color.White', 'tint = Color.Black')

# Fix color = Color.White
content = content.replace('color = Color.White', 'color = Color.Black')

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
