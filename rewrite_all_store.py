import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Fix DataRechargeCard Signature
content = re.sub(
    r'fun DataRechargeCard\(\n    plan: DataRechargePlan,\n    userCoins: Int,\n    isLoading: Boolean(?: = false)?,\n    onRedeemClick: \(DataRechargePlan\) -> Unit\n\)',
    'fun DataRechargeCard(\n    plan: DataRechargePlan,\n    userCoins: Int,\n    isLoading: Boolean = false,\n    onRedeemClick: (DataRechargePlan) -> Unit\n)',
    content
)

# Fix FamPayGooglePlayCard canAfford
content = re.sub(
    r'(fun FamPayGooglePlayCard[\s\S]*?\{[\s\S]*?)val canAfford = userCoins >= plan\.coinPrice && !isLoading',
    r'\1val canAfford = userCoins >= effectivePrice && isEnabled',
    content
)

content = re.sub(
    r'(fun FamPayGooglePlayCard[\s\S]*?\.clickable.*?)\{ onRedeemClick\(plan\) \}',
    r'\1{ onBuyClick() }',
    content
)

content = re.sub(
    r'(fun FamPayGooglePlayCard[\s\S]*?)\.clickable\(enabled = canAfford && !isLoading\)',
    r'\1.clickable(enabled = canAfford && !isOutOfStock)',
    content
)

# Fix TournamentDiscountCard canAfford
content = re.sub(
    r'(fun TournamentDiscountCard[\s\S]*?\{[\s\S]*?)val canAfford = userCoins >= plan\.coinPrice && !isLoading',
    r'\1val canAfford = userCoins >= effectivePrice && isEnabled',
    content
)

content = re.sub(
    r'(fun TournamentDiscountCard[\s\S]*?\.clickable.*?)\{ onRedeemClick\(plan\) \}',
    r'\1{ onBuyClick() }',
    content
)

content = re.sub(
    r'(fun TournamentDiscountCard[\s\S]*?)\.clickable\(enabled = canAfford && !isLoading\)',
    r'\1.clickable(enabled = canAfford && !isOutOfStock)',
    content
)


# Fix DataRechargeCard missing variables
content = re.sub(
    r'(fun DataRechargeCard\([\s\S]*?\{)[\s\S]*?val sheenX by infiniteTransition',
    r'\1\n    val canAfford = userCoins >= plan.coinPrice && !isLoading\n    val isJio = plan.operator.equals("JIO", ignoreCase = true)\n\n    val infiniteTransition = rememberInfiniteTransition(label = "darkSheen")\n    val sheenX by infiniteTransition',
    content
)
# Make sure DataRechargeCard uses the right click listener
content = re.sub(
    r'(fun DataRechargeCard[\s\S]*?)\.clickable\(enabled = canAfford\) \{ onBuyClick\(\) \}',
    r'\1.clickable(enabled = canAfford && !isLoading) { onRedeemClick(plan) }',
    content
)

# DataRechargeCard replacements for plan
content = re.sub(
    r'(fun DataRechargeCard[\s\S]*?)if \(isJio\) "JIO DATA" else "AIRTEL DATA"',
    r'\1"${plan.operator} DATA"',
    content
)
content = re.sub(
    r'(fun DataRechargeCard[\s\S]*?)item\.description',
    r'\1plan.dataAmount',
    content
)
content = re.sub(
    r'(fun DataRechargeCard[\s\S]*?)item\.title\.uppercase\(\)',
    r'\1("${plan.operator} PACK - ${plan.validity}").uppercase()',
    content
)
content = re.sub(
    r'(fun DataRechargeCard[\s\S]*?)"\$effectivePrice COINS"',
    r'\1"${plan.coinPrice} COINS"',
    content
)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

