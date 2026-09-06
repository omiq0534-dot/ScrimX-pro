import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# 1. MonetizationOn -> Star
content = content.replace("Icons.Default.MonetizationOn", "Icons.Default.Star")

# 2. Emojis
content = content.replace("⚡ Selected:", "SELECTED:")
content = content.replace("✅ Valid Indian mobile number", "VALID INDIAN MOBILE NUMBER")
content = content.replace("⚠️ ", "")

# 3. Insert Vault Category Filter
vault_original = '''3 -> {
                    // My Vault / Purchased Cards
                    if (purchasedCards.isEmpty()) {'''
                    
vault_replacement = '''3 -> {
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

                    if (filteredVault.isEmpty()) {'''
                    
content = content.replace(vault_original, vault_replacement)

# 4. Replace purchasedCards with filteredVault in LazyColumn
content = content.replace('items(purchasedCards) { card ->', 'items(filteredVault) { card ->')
content = content.replace('Text("${purchasedCards.size} Items"', 'Text("${filteredVault.size} Items"')

# 5. Fix the closing brace for the newly added `Column` in Vault.
# Original code around end of vault:
#                             item { Spacer(modifier = Modifier.height(40.dp)) }
#                         }
#                     }
#                 }
#             }
#         }
#     }
# }
# // ---------
end_vault = '''                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }'''
end_vault_replacement = '''                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                    }'''
content = content.replace(end_vault, end_vault_replacement)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

