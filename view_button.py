import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

match = re.search(r'Button\(\s*onClick = \{\s*if \(\!isBought\) \{.*?\}(?=\n\s*\}\n\s*\})', content, flags=re.MULTILINE | re.DOTALL)
if match:
    print(match.group(0))

