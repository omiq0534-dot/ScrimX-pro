import re
with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

vault = re.search(r'(@Composable\s*fun PurchasedCardVaultItem.*?)(?=fun CelebrationCardDialog)', content, flags=re.MULTILINE | re.DOTALL)
if vault:
    print(vault.group(1))

