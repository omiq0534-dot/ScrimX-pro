import re

with open('store_latest.kt', 'r') as f:
    content = f.read()

# 1. Add missing imports
if "import androidx.compose.ui.graphics.BlendMode" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", "import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.BlendMode\nimport androidx.compose.animation.*")

# 2. Fix line 709 & 1190 & 1204 - `plan` instead of `item` (in FamPay and Tournament Cards?)
# Wait, let's just see where `plan` is used instead of `item` outside of `DataRechargeCard`.
# Actually, it's easier to find the context. Let's do regex replaces.

# In FamPayGooglePlayCard:
fampay_regex = r'(fun FamPayGooglePlayCard[\s\S]*?)(?=fun TournamentDiscountCard|fun PurchasedCardVaultItem)'
def fix_fampay(match):
    text = match.group(1)
    text = text.replace('{ onRedeemClick(plan) }', '{ onBuyClick() }')
    text = text.replace('val canAfford = userCoins >= plan.coinPrice', 'val canAfford = userCoins >= effectivePrice')
    text = text.replace('!isLoading', 'isEnabled')
    return text
content = re.sub(fampay_regex, fix_fampay, content)

# In TournamentDiscountCard:
tourn_regex = r'(fun TournamentDiscountCard[\s\S]*?)(?=fun PurchasedCardVaultItem|fun StoreHeroBanner)'
def fix_tourn(match):
    text = match.group(1)
    text = text.replace('{ onRedeemClick(plan) }', '{ onBuyClick() }')
    text = text.replace('val canAfford = userCoins >= plan.coinPrice', 'val canAfford = userCoins >= effectivePrice')
    text = text.replace('!isLoading', 'isEnabled')
    return text
content = re.sub(tourn_regex, fix_tourn, content)

# In DataRechargeCard:
# Ensure isOutOfStock and onBuyClick are not used here
data_regex = r'(fun DataRechargeCard[\s\S]*?)(?=fun DataRechargeDialog|$)'
def fix_data(match):
    text = match.group(1)
    text = text.replace('{ onBuyClick() }', '{ onRedeemClick(plan) }')
    text = text.replace('enabled = canAfford && !isOutOfStock', 'enabled = canAfford && !isLoading')
    return text
content = re.sub(data_regex, fix_data, content)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
