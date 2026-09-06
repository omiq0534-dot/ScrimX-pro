import re
with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

funcs = [
    "fun StoreItemCard",
    "fun FamPayGooglePlayCard",
    "fun TournamentDiscountCard",
    "fun PurchasedCardVaultItem"
]

for func in funcs:
    match = re.search(r'(@Composable\s*' + func + r'.*?^})', content, flags=re.MULTILINE | re.DOTALL)
    if match:
        print(f"=== {func} Found ===")
        # Print just the signature and first few lines
        lines = match.group(1).split('\n')
        print('\n'.join(lines[:15]))
