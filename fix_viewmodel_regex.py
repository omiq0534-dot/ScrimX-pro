import re

with open('app/src/main/java/com/example/ui/screens/StoreViewModel.kt', 'r') as f:
    content = f.read()

pattern = r'val appDiscountCards = listOf\(.*?(?=val jioRechargePlans = listOf\()'
new_discount = """val appDiscountCards = listOf(
        StoreItem(
            id = "disc_flat_10",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Silver Ticket",
            subtitle = "Save ₹10 on your next 3 match entries",
            coinPrice = 75,
            discountFlatRupees = 10,
            maxUses = 3,
            cardColorTheme = "SILVER",
            badgeText = "SAVE ₹30 TOTAL",
            description = "Save ₹10 on every match you join for 3 consecutive entries."
        ),
        StoreItem(
            id = "disc_pct_25",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Gold Ticket",
            subtitle = "25% discount on all entry fees",
            coinPrice = 110,
            discountPercent = 25,
            maxUses = 5,
            cardColorTheme = "GOLD",
            badgeText = "5 MATCHES VIP",
            description = "Get 25% OFF on up to 5 tournament entry tickets."
        ),
        StoreItem(
            id = "disc_pct_50",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Diamond Ticket",
            subtitle = "Play top tournaments at half price",
            coinPrice = 190,
            discountPercent = 50,
            maxUses = 3,
            cardColorTheme = "DIAMOND",
            badgeText = "HALF PRICE 🔥",
            description = "Pay only 50% entry fee on 3 mega tournaments."
        )
    )
    
    // JIO Official Data Booster Plans (2024-2026 Latest)
    """

content = re.sub(pattern, new_discount, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/StoreViewModel.kt', 'w') as f:
    f.write(content)
