import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

old = """fun TournamentDiscountCard(
    item: StoreItem,
    effectivePrice: Int = item.coinPrice,
    isEnabled: Boolean = true,
    dailyLimit: Int = 1,
    customCodesCount: Int = 0,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit,
    onRequestRestock: () -> Unit
) {
    val textPrimaryColor = AppColors.TextPrimary"""

new = """fun TournamentDiscountCard(
    item: StoreItem,
    effectivePrice: Int = item.coinPrice,
    isEnabled: Boolean = true,
    dailyLimit: Int = 1,
    customCodesCount: Int = 0,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit,
    onRequestRestock: () -> Unit
) {
    val textPrimaryColor = AppColors.TextPrimary
    val borderColor = AppColors.BorderColor"""

content = content.replace(old, new)
content = content.replace("AppColors.BorderColor", "borderColor", 1) # Just for the 1st occurrence if I do it blindly, but let's just replace all inside the file carefully.

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)

