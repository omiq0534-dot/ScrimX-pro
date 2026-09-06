import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Fix the condition `if (purchasedCards.isEmpty())` -> `if (filteredVault.isEmpty())`
content = content.replace('if (purchasedCards.isEmpty()) {', 'if (filteredVault.isEmpty()) {')

# Fix the `items(purchasedCards) { card ->` inside LazyColumn to use `filteredVault`
content = content.replace('items(purchasedCards) { card ->', 'items(filteredVault) { card ->')

# Check if Column is correctly closed.
# We had `3 -> { \n var selectedVaultCat... Column() { ScrollableTabRow... if(filteredVault.isEmpty()) { ... } else { LazyColumn() { ... } } `
# Let's see if we added an extra brace at the end of LazyColumn.
lazy_block = r'(items\(filteredVault\) \{ card ->[\s\S]*?PurchasedCardVaultItem\([\s\S]*?onMarkUsed = \{.*?\}.*?\)\n\s*\})([\s\S]*?)(item \{ Spacer.*?\}\s*\})'
content = re.sub(lazy_block, r'\1\2\3\n                    }', content)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
