import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Fix DataRechargeCard signature
content = content.replace(
'''fun DataRechargeCard(
    plan: DataRechargePlan,
    userCoins: Int,
    isLoading: Boolean,
    onRedeemClick: (DataRechargePlan) -> Unit
)''',
'''fun DataRechargeCard(
    plan: DataRechargePlan,
    userCoins: Int,
    isLoading: Boolean = false,
    onRedeemClick: (DataRechargePlan) -> Unit
)'''
)

# Fix FamPayGooglePlayCard
content = content.replace(
'''fun FamPayGooglePlayCard(
    item: StoreItem,
    effectivePrice: Int = item.coinPrice,
    isEnabled: Boolean = true,
    dailyLimit: Int = 2,
    customCodesCount: Int = 0,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit,
    onRequestRestock: () -> Unit
) {
    val canAfford = userCoins >= plan.coinPrice && !isLoading''',
'''fun FamPayGooglePlayCard(
    item: StoreItem,
    effectivePrice: Int = item.coinPrice,
    isEnabled: Boolean = true,
    dailyLimit: Int = 2,
    customCodesCount: Int = 0,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit,
    onRequestRestock: () -> Unit
) {
    val canAfford = userCoins >= effectivePrice && isEnabled'''
)

content = content.replace(
'''
                    if (isOutOfStock) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF262626))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .clickable(enabled = canAfford && !isLoading) { onRedeemClick(plan) }
''',
'''
                    if (isOutOfStock) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF262626))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .clickable { onRequestRestock() }
'''
)

# Wait, `content.replace('.clickable(enabled = canAfford) { onBuyClick() }', '.clickable(enabled = canAfford && !isLoading) { onRedeemClick(plan) }')`
# This might have affected everything that used `.clickable(enabled = canAfford)`. Let's just fix FamPay, Tournament and Vault again completely.
