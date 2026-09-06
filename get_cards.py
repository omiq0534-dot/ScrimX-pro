import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

play = re.search(r'(@Composable\s*fun FamPayGooglePlayCard.*?^})', content, flags=re.MULTILINE | re.DOTALL)
if play:
    print("=== FamPayGooglePlayCard ===")
    print(play.group(1)[:1500])

vault = re.search(r'(@Composable\s*fun PurchasedCardVaultItem.*?^})', content, flags=re.MULTILINE | re.DOTALL)
if vault:
    print("=== PurchasedCardVaultItem ===")
    print(vault.group(1)[:2000])

