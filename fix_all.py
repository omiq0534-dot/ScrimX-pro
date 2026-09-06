import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Fix TournamentDiscountCard
content = content.replace(
'''fun TournamentDiscountCard(
    item: StoreItem,
    effectivePrice: Int,
    isEnabled: Boolean,
    dailyLimit: Int,
    customCodesCount: Int,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit
) {
    val canAfford = userCoins >= plan.coinPrice && !isLoading''',
'''fun TournamentDiscountCard(
    item: StoreItem,
    effectivePrice: Int,
    isEnabled: Boolean,
    dailyLimit: Int,
    customCodesCount: Int,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit
) {
    val canAfford = userCoins >= effectivePrice && isEnabled'''
)

# Any stray plan.coinPrice && !isLoading in FamPay or Tournament clickables
fampay_regex = r'@Composable\s*fun FamPayGooglePlayCard.*?(?=@Composable)'
tourn_regex = r'@Composable\s*fun TournamentDiscountCard.*?(?=@Composable)'

def fix_clickable(text):
    return text.group(0).replace('.clickable(enabled = canAfford && !isLoading) { onRedeemClick(plan) }', '.clickable(enabled = canAfford && !isOutOfStock) { onBuyClick() }')

content = re.sub(fampay_regex, fix_clickable, content, flags=re.MULTILINE|re.DOTALL)
content = re.sub(tourn_regex, fix_clickable, content, flags=re.MULTILINE|re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

