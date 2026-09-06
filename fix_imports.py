import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
import androidx.compose.animation.*
import androidx.compose.ui.graphics.BlendMode
"""
content = re.sub(r'import androidx.compose.runtime.Composable', imports + '\nimport androidx.compose.runtime.Composable', content)

# Fix StoreItemCard call vs DataRechargeCard
# e: file:///app/src/main/java/com/example/ui/screens/StoreScreen.kt:426:33 No parameter with name 'plan' found.
# Let's search around line 426. The original had:
# DataRechargeCard(plan = pack, userCoins = coins, isLoading = false, onRedeemClick = { selectedPlan = it; showDialog = true })
# Oh, the original function was DataRechargeCard(plan: DataRechargePlan, userCoins: Int, isLoading: Boolean, onRedeemClick: (DataRechargePlan) -> Unit)
# I overwrote DataRechargeCard with a signature matching StoreItemCard. Let me fix the signature of DataRechargeCard.

old_data_card = r'@Composable\nfun DataRechargeCard\(\n    item: StoreItem,\n    effectivePrice: Int,\n    isEnabled: Boolean,\n    dailyLimit: Int,\n    customCodesCount: Int,\n    userName: String,\n    userCoins: Int,\n    onBuyClick: \(\) -> Unit\n\)'

new_data_card = """@Composable
fun DataRechargeCard(
    plan: DataRechargePlan,
    userCoins: Int,
    isLoading: Boolean,
    onRedeemClick: (DataRechargePlan) -> Unit
)"""
content = re.sub(old_data_card, new_data_card, content)

# Also fix the usages inside DataRechargeCard
# item.title -> plan.operator + " " + plan.dataAmount
# effectivePrice -> plan.coinPrice
# canAfford -> userCoins >= plan.coinPrice
# item.description -> plan.validity
# onBuyClick -> onRedeemClick(plan)
# isEnabled -> !isLoading

content = content.replace('val canAfford = userCoins >= effectivePrice && isEnabled', 'val canAfford = userCoins >= plan.coinPrice && !isLoading')
content = content.replace('val isJio = item.title.contains("JIO", ignoreCase = true)', 'val isJio = plan.operator.equals("JIO", ignoreCase = true)')
content = content.replace('.clickable(enabled = canAfford) { onBuyClick() }', '.clickable(enabled = canAfford && !isLoading) { onRedeemClick(plan) }')
content = content.replace('item.description, // usually "1 GB" etc.', 'plan.dataAmount,')
content = content.replace('item.title.uppercase(),', '("${plan.operator} PACK - ${plan.validity}").uppercase(),')
content = content.replace('"$effectivePrice COINS"', '"${plan.coinPrice} COINS"')

# Fix FamPayGooglePlayCard item.rewardAmount
content = content.replace('item.rewardAmount', 'item.title.replace("Google Play ", "").replace(" Gift Card", "")')

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

