with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()
import re
matches = re.finditer(r'items\(storeViewModel\.(.*?)\)', content)
for m in matches:
    print(m.group(0))
