import re

with open('app/src/main/java/com/example/ui/screens/StoreViewModel.kt', 'r') as f:
    content = f.read()

# Replace appDiscountCards
old_discount = """    val appDiscountCards = listOf(
        StoreItem(
            id = "disc_flat_10",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Silver Flat ₹10 OFF Card",
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
            title = "Gold 25% OFF VIP Pass",
            subtitle = "25% discount on all entry fees",
            coinPrice = 110,
            discountPercent = 25,
            maxUses = 5,
            cardColorTheme = "GOLD",
            badgeText = "5 MATCHES VIP",
            description = "Get 25% OFF on up to 5 tournament entry tickets. High savings on mega prize pools!"
        ),
        StoreItem(
            id = "disc_pct_50",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Diamond 50% Half-Price Pass",
            subtitle = "Play top tournaments at half price",
            coinPrice = 190,
            discountPercent = 50,
            maxUses = 3,
            cardColorTheme = "DIAMOND",
            badgeText = "HALF PRICE 🔥",
            description = "Pay only 50% entry fee on 3 mega tournaments. Top choice for competitive esports players!"
        ),
        StoreItem(
            id = "disc_free_100",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "God-Tier 100% Free Pass",
            subtitle = "100% Free Entry for any 1 Match",
            coinPrice = 280,
            discountPercent = 100,
            maxUses = 1,
            cardColorTheme = "DIAMOND", // Or custom
            badgeText = "FREE ENTRY",
            description = "Completely waives off the entry fee for any 1 premium tournament match."
        )
    )"""

new_discount = """    val appDiscountCards = listOf(
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
            description = "Get 25% OFF on up to 5 tournament entry tickets. High savings on mega prize pools!"
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
            description = "Pay only 50% entry fee on 3 mega tournaments. Top choice for competitive esports players!"
        )
    )"""

if old_discount in content:
    content = content.replace(old_discount, new_discount)
else:
    print("Could not find old_discount")

with open('app/src/main/java/com/example/ui/screens/StoreViewModel.kt', 'w') as f:
    f.write(content)
