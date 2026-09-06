import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# 1. Icons.Default.MonetizationOn -> Icons.Default.Star
content = content.replace("Icons.Default.MonetizationOn", "Icons.Default.Star")

# 2. Emojis
content = content.replace("⚡ Selected:", "SELECTED:")
content = content.replace("✅ Valid Indian mobile number", "VALID INDIAN MOBILE NUMBER")
content = content.replace("⚠️ ", "")
content = content.replace("महत्वपूर्ण सूचना / Notice", "NOTICE")
content = content.replace("अगर मोबाइल नंबर गलत हुआ तो यूजर की खुद की जिम्मेदारी होगी।", "User is responsible for entering the correct mobile number.")
content = content.replace("कृपया अपना 10-अंकों का नंबर सही से जांच लें। रिचार्ज की पुष्टि रसीद/स्क्रीनशॉट एडमिन द्वारा प्रदान की जाएगी।", "Please verify your 10-digit number. Receipt/Screenshot will be provided by admin.")


# 3. Vault Tabs
vault_start = content.find("3 -> {\n                    // My Vault / Purchased Cards")
if vault_start != -1:
    vault_end = content.find("if (purchasedCards.isEmpty()) {", vault_start)
    if vault_end != -1:
        new_vault_header = '''3 -> {
                    // My Vault / Purchased Cards
                    var selectedVaultCat by remember { mutableIntStateOf(0) }
                    
                    val filteredVault = purchasedCards.filter {
                        when (selectedVaultCat) {
                            1 -> it.category.equals("DATA_RECHARGE", ignoreCase = true)
                            2 -> it.category.equals("GOOGLE_PLAY", ignoreCase = true)
                            3 -> !it.category.equals("DATA_RECHARGE", ignoreCase = true) && !it.category.equals("GOOGLE_PLAY", ignoreCase = true)
                            else -> true
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedVaultCat,
                            containerColor = Color.Transparent,
                            divider = {},
                            edgePadding = 16.dp,
                            indicator = {}
                        ) {
                            listOf("ALL", "RECHARGE", "GOOGLE PLAY", "TICKET").forEachIndexed { idx, title ->
                                val isCatSelected = selectedVaultCat == idx
                                Tab(
                                    selected = isCatSelected,
                                    onClick = { selectedVaultCat = idx },
                                    modifier = Modifier.padding(end = 8.dp, bottom = 12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCatSelected) Color.Black else Color(0xFFF3F4F6))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            title,
                                            color = if (isCatSelected) Color.White else Color(0xFF8E93A6),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }

                        '''
        content = content[:vault_start] + new_vault_header + content[vault_end:]

# 4. Modify Vault empty condition closing and LazyColumn
content = content.replace(
    'items(purchasedCards.reversed()) { card ->',
    'items(filteredVault.reversed()) { card ->'
)
# We need to add a `}` after the vault LazyColumn because we wrapped everything in `Column { ... }`.
lazy_column_end_regex = r'(items\(filteredVault\.reversed\(\)\) \{ card ->[\s\S]*?PurchasedCardVaultItem\([\s\S]*?onMarkUsed = \{.*?\}.*?\)\n\s*\}\n\s*item \{ Spacer\(modifier = Modifier\.height\(40\.dp\)\) \}\n\s*\})'
content = re.sub(lazy_column_end_regex, r'\1\n                    }', content)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
