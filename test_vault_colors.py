import re
with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

vault = re.search(r'(@Composable\s*fun PurchasedCardVaultItem.*?)(?=fun CelebrationCardDialog)', content, flags=re.MULTILINE | re.DOTALL)
if vault:
    vault_str = vault.group(1)
    for i, line in enumerate(vault_str.split('\n')):
        if 'Color(0xFF' in line:
            print(f"{i}: {line.strip()}")
