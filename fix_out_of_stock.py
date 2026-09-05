import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Add request restock to card parameters
content = re.sub(
    r'@Composable\s*fun FamPayGooglePlayCard\(\s*item: StoreItem,\s*effectivePrice: Int = item.coinPrice,\s*isEnabled: Boolean = true,\s*dailyLimit: Int = 2,\s*customCodesCount: Int = 0,\s*userName: String,\s*userCoins: Int,\s*onBuyClick: \(\) -> Unit\s*\)',
    """@Composable
fun FamPayGooglePlayCard(
    item: StoreItem,
    effectivePrice: Int = item.coinPrice,
    isEnabled: Boolean = true,
    dailyLimit: Int = 2,
    customCodesCount: Int = 0,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit,
    onRequestRestock: () -> Unit
)""", content, flags=re.MULTILINE
)

# Replace Button in FamPayGooglePlayCard
old_button = """        Button(
            onClick = { 
                if (!isBought) {
                    isBought = true
                    onBuyClick() 
                }
            },
            enabled = (canAfford || isBought),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isBought) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFFFD700),
                disabledContainerColor = if (isBought) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFF1E2338)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .scale(scale)
                .border(
                    1.dp,
                    if (isBought) Color(0xFF22C55E) else Color.Transparent,
                    RoundedCornerShape(14.dp)
                )
        ) {
            if (isBought) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "CLAIMED SUCCESSFULLY",
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            } else {
                Text(
                    "REDEEM FOR",
                    color = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "$effectivePrice",
                    color = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }"""

new_button = """        var hasRequestedRestock by remember { mutableStateOf(false) }
        val isOutOfStock = customCodesCount <= 0

        Button(
            onClick = { 
                if (isOutOfStock && !hasRequestedRestock) {
                    hasRequestedRestock = true
                    onRequestRestock()
                } else if (!isOutOfStock && !isBought) {
                    isBought = true
                    onBuyClick() 
                }
            },
            enabled = (canAfford || isBought || isOutOfStock) && !hasRequestedRestock,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isBought) Color(0xFF22C55E).copy(alpha = 0.15f) else if (isOutOfStock) Color(0xFFE11D48).copy(alpha = 0.15f) else Color(0xFFFFD700),
                disabledContainerColor = if (isBought) Color(0xFF22C55E).copy(alpha = 0.15f) else if (isOutOfStock) Color(0xFFE11D48).copy(alpha = 0.15f) else Color(0xFF1E2338)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .scale(scale)
                .border(
                    1.dp,
                    if (isBought) Color(0xFF22C55E) else if (isOutOfStock) Color(0xFFE11D48).copy(alpha = 0.5f) else Color.Transparent,
                    RoundedCornerShape(14.dp)
                )
        ) {
            if (isBought) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "CLAIMED SUCCESSFULLY",
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            } else if (isOutOfStock) {
                if (hasRequestedRestock) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "RESTOCK REQUESTED",
                        color = Color(0xFFE11D48),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "OUT OF STOCK - REQUEST ADMIN",
                        color = Color(0xFFE11D48),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                Text(
                    "REDEEM FOR",
                    color = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "$effectivePrice",
                    color = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }"""
content = content.replace(old_button, new_button)

# Also update the call to FamPayGooglePlayCard
old_call = """                            FamPayGooglePlayCard(
                                item = item,
                                effectivePrice = effectivePrice,
                                isEnabled = isItemActive,
                                dailyLimit = dailyLimit,
                                customCodesCount = codesCount,
                                userName = userName,
                                userCoins = userCoins,
                                onBuyClick = { selectedItemForPurchase = item }
                            )"""
new_call = """                            FamPayGooglePlayCard(
                                item = item,
                                effectivePrice = effectivePrice,
                                isEnabled = isItemActive,
                                dailyLimit = dailyLimit,
                                customCodesCount = codesCount,
                                userName = userName,
                                userCoins = userCoins,
                                onBuyClick = { selectedItemForPurchase = item },
                                onRequestRestock = { 
                                    storeViewModel.requestRestock(item, 
                                        onSuccess = { Toast.makeText(context, "Restock request sent to Admin!", Toast.LENGTH_SHORT).show() },
                                        onError = { Toast.makeText(context, "Failed to send request.", Toast.LENGTH_SHORT).show() }
                                    ) 
                                }
                            )"""
content = content.replace(old_call, new_call)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

