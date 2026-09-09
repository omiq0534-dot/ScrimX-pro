with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# Fix in FamPayGooglePlayCard
# find "plan.coinPrice" and replace with "effectivePrice"
# Wait, I'll just use regex or replace safely.

# For TournamentDiscountCard
content = content.replace('"${plan.operator} PACK - ${plan.validity}"', 'item.title')
content = content.replace('${plan.coinPrice}', '${effectivePrice}')

# For FamPayGooglePlayCard
content = content.replace('plan.coinPrice', 'effectivePrice')

# Also replace isOutOfStock unresolved reference in DataRechargeCard
content = content.replace('val buttonText = if (isOutOfStock) "OUT OF STOCK" else "REDEEM NOW"', 
                          'val buttonText = "REDEEM NOW"')
content = content.replace('val buttonColor = if (isOutOfStock) Color(0xFF333333) else Color(0xFF00E5FF)',
                          'val buttonColor = Color(0xFF00E5FF)')
content = content.replace('val buttonTextColor = if (isOutOfStock) Color(0xFF888888) else Color(0xFF0A0B10)',
                          'val buttonTextColor = Color(0xFF0A0B10)')

content = content.replace('onClick = onBuyClick', 'onClick = { onRedeemClick(plan) }')

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)
