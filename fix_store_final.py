import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# I want to define `val canvasColor = AppColors.TextPrimary` globally? No, that's `@Composable`.
# I'll replace `canvasColor` back to `AppColors.TextPrimary`.
content = content.replace("canvasColor", "AppColors.TextPrimary")
content = content.replace("val AppColors.TextPrimary = AppColors.TextPrimary\n        ", "")

# Now I will define `val textPrimaryColor = AppColors.TextPrimary` at the top of the functions that use Canvas:
# 1. StoreVIPPassCard
old_vip = "fun StoreVIPPassCard(modifier: Modifier = Modifier, onClick: () -> Unit) {"
new_vip = "fun StoreVIPPassCard(modifier: Modifier = Modifier, onClick: () -> Unit) {\n    val textPrimaryColor = AppColors.TextPrimary"
content = content.replace(old_vip, new_vip)

# 2. RedeemCodeCard
old_redeem = "fun RedeemCodeCard(modifier: Modifier = Modifier, onClick: () -> Unit) {"
new_redeem = "fun RedeemCodeCard(modifier: Modifier = Modifier, onClick: () -> Unit) {\n    val textPrimaryColor = AppColors.TextPrimary"
content = content.replace(old_redeem, new_redeem)

# 3. GameCurrencyCard
old_currency = """fun GameCurrencyCard(
    title: String,
    price: String,
    oldPrice: String? = null,
    discount: String? = null,
    imageAsset: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconContent: @Composable () -> Unit
) {"""
new_currency = """fun GameCurrencyCard(
    title: String,
    price: String,
    oldPrice: String? = null,
    discount: String? = null,
    imageAsset: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconContent: @Composable () -> Unit
) {
    val textPrimaryColor = AppColors.TextPrimary"""
content = content.replace(old_currency, new_currency)

# Now selectively replace `AppColors.TextPrimary` inside Canvas blocks to `textPrimaryColor`.
# To be safe, I'll just replace `AppColors.TextPrimary.copy` with `textPrimaryColor.copy`
content = content.replace("AppColors.TextPrimary.copy", "textPrimaryColor.copy")

# And fix line 1384 & 1408 `border(1.dp, AppColors.TextPrimary.copy(...)` is not in canvas, it's outside. But `textPrimaryColor` is not defined there!
# Those are in `JioBrandLogo` and `AirtelBrandLogo`.
# Let's add `val textPrimaryColor = AppColors.TextPrimary` to them too.
old_jio = "fun JioBrandLogo(modifier: Modifier = Modifier, size: Int = 36) {"
new_jio = "fun JioBrandLogo(modifier: Modifier = Modifier, size: Int = 36) {\n    val textPrimaryColor = AppColors.TextPrimary"
content = content.replace(old_jio, new_jio)

old_airtel = "fun AirtelBrandLogo(modifier: Modifier = Modifier, size: Int = 36) {"
new_airtel = "fun AirtelBrandLogo(modifier: Modifier = Modifier, size: Int = 36) {\n    val textPrimaryColor = AppColors.TextPrimary"
content = content.replace(old_airtel, new_airtel)


with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)

