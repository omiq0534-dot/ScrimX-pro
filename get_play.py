import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

play = re.search(r'(@Composable\s*fun FamPayGooglePlayCard.*?^})', content, flags=re.MULTILINE | re.DOTALL)
if play:
    print(play.group(1))

