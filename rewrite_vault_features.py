import re

with open('temp_store.kt', 'r') as f:
    content = f.read()

# 1. MonetizationOn -> Star
content = content.replace("Icons.Default.MonetizationOn", "Icons.Default.Star")

# 2. Emojis removal
content = content.replace("⚡ Selected:", "SELECTED:")
content = content.replace("✅ Valid Indian mobile number", "VALID INDIAN MOBILE NUMBER")
content = content.replace("⚠️ ", "")
content = content.replace("महत्वपूर्ण सूचना / Notice", "NOTICE")
content = content.replace("अगर मोबाइल नंबर गलत हुआ तो यूजर की खुद की जिम्मेदारी होगी।", "User is responsible for entering the correct mobile number.")
content = content.replace("कृपया अपना 10-अंकों का नंबर सही से जांच लें। रिचार्ज की पुष्टि रसीद/स्क्रीनशॉट एडमिन द्वारा प्रदान की जाएगी।", "Please verify your 10-digit number. Receipt/Screenshot will be provided by admin.")


# 3. Add Vault Category State & Filter logic
vault_regex = r'(3 -> \{\s*// My Vault / Purchased Cards)'
vault_replacement = '''3 -> {
                    // My Vault / Purchased Cards
                    var selectedVaultCat by remember { mutableIntStateOf(0) } // 0: All, 1: Recharge, 2: Google Play, 3: Scrim X Ticket
                    
                    val filteredVault = purchasedCards.filter {
                        when (selectedVaultCat) {
                            1 -> it.category.equals("DATA_RECHARGE", ignoreCase = true)
                            2 -> it.category.equals("GOOGLE_PLAY", ignoreCase = true)
                            3 -> !it.category.equals("DATA_RECHARGE", ignoreCase = true) && !it.category.equals("GOOGLE_PLAY", ignoreCase = true)
                            else -> true
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Vault Category Tabs
                        ScrollableTabRow(
                            selectedTabIndex = selectedVaultCat,
                            containerColor = Color.Transparent,
                            divider = {},
                            edgePadding = 16.dp,
                            indicator = {}
                        ) {
                            listOf("ALL", "RECHARGE", "GOOGLE PLAY", "SCRIM X TICKET").forEachIndexed { idx, title ->
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

content = re.sub(r'3 -> \{\s*// My Vault / Purchased Cards[\s\S]*?if \(purchasedCards\.isEmpty\(\)\) \{', vault_replacement, content)

# 4. Replace `purchasedCards.reversed()` with `filteredVault.reversed()` inside the LazyColumn for Vault
lazy_column_regex = r'LazyColumn\([\s\S]*?items\(purchasedCards\.reversed\(\)\) \{ card ->'
content = re.sub(lazy_column_regex, 'LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {\n                            items(filteredVault.reversed()) { card ->', content)

# Also need to close the `Column` block added for Vault Tabs.
# The original code looks like:
#                             }
#                         }
#                         item { Spacer(modifier = Modifier.height(40.dp)) }
#                     }
#                 }
#             }
# We need to insert `}` right after the `LazyColumn` ends. Let's do this dynamically by finding where it ends.
# Better way: replace `item { Spacer(modifier = Modifier.height(40.dp)) }\n                    }` with `item { Spacer(modifier = Modifier.height(40.dp)) }\n                    }\n                    }` but only for the vault block.
# Actually I'll use regex to inject the closing brace for Column.
content = re.sub(
    r'(items\(filteredVault\.reversed\(\)\) \{ card ->[\s\S]*?PurchasedCardVaultItem\([\s\S]*?\)\s*\})([\s\S]*?)(item \{ Spacer.*?\}\s*\})',
    r'\1\2\3\n                    }',
    content
)


# 5. Rewrite PurchasedCardVaultItem completely
vault_item_func_regex = r'@Composable\s*fun PurchasedCardVaultItem\(.*?(?=\n@Composable|\n// ---|\Z)'

new_vault_item = """@Composable
fun PurchasedCardVaultItem(
    card: UserPurchasedCard,
    userName: String,
    onMarkUsed: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var isRevealed by remember { mutableStateOf(false) }

    val isDataRecharge = card.category.equals("DATA_RECHARGE", ignoreCase = true)
    val isUsed = card.status.equals("USED", ignoreCase = true)

    val proofBase64String = card.proofScreenshotBase64.ifBlank { card.screenshotBase64 }
    val effectiveRechargeTxnId = card.rechargeTxnId.ifBlank { card.operatorTxnId }

    var showProofDialog by remember { mutableStateOf(false) }

    val proofBitmap = remember(proofBase64String) {
        if (proofBase64String.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(proofBase64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (showProofDialog && proofBitmap != null) {
        Dialog(onDismissRequest = { showProofDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recharge Confirmation", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("Official receipt from admin", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showProofDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                        }
                    }

                    Image(
                        bitmap = proofBitmap.asImageBitmap(),
                        contentDescription = "Recharge Proof Screenshot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )

                    if (effectiveRechargeTxnId.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE5E7EB))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ref ID / UTR: $effectiveRechargeTxnId", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { showProofDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("CLOSE PROOF", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Main Card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111)) // Titanium Black Base
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(16.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    card.category.replace("_", " ").uppercase(),
                    color = Color(0xFFA0AAB2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    card.title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, if (isUsed) Color(0xFF444444) else Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isUsed) "CLAIMED" else "ACTIVE",
                    color = if (isUsed) Color(0xFF444444) else Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        // Body Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isDataRecharge) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "STATUS",
                        color = Color(0xFF555555),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        if (proofBitmap != null) "SUCCESSFUL" else "PENDING",
                        color = if (proofBitmap != null) Color(0xFFE2E8F0) else Color(0xFF8E93A6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "REDEEM CODE",
                        color = Color(0xFF555555),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                }

                // Blur container for code
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val codeToDisplay = card.code.ifBlank { "PENDING..." }
                    
                    Text(
                        text = codeToDisplay,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 4.sp,
                        modifier = Modifier.then(
                            if (!isRevealed && card.code.isNotBlank()) androidx.compose.ui.Modifier.blur(radius = 8.dp) else androidx.compose.ui.Modifier
                        )
                    )
                }
            }

            // Two Black Buttons at Bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isDataRecharge) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                            .clickable { Toast.makeText(context, "Reported to Admin", Toast.LENGTH_SHORT).show() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("REPORT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                            .clickable(enabled = proofBitmap != null) {
                                if (proofBitmap != null) showProofDialog = true
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SCREENSHOT", color = if (proofBitmap != null) Color.White else Color(0xFF555555), fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                } else {
                    // Code Redeem Buttons
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                            .clickable { Toast.makeText(context, "Reported to Admin", Toast.LENGTH_SHORT).show() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("REPORT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                            .clickable(enabled = card.code.isNotBlank()) {
                                if (card.code.isNotBlank()) {
                                    if (!isRevealed) {
                                        isRevealed = true
                                        onMarkUsed() // Optional: Mark as used automatically when revealed
                                    } else {
                                        clipboardManager.setPrimaryClip(ClipData.newPlainText("Code", card.code))
                                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (!isRevealed) "CLAIM" else "COPY", color = if (card.code.isNotBlank()) Color.White else Color(0xFF555555), fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                }
            }
        }
    }
}
"""

content = re.sub(vault_item_func_regex, new_vault_item, content, flags=re.MULTILINE | re.DOTALL)

with open('temp_store2.kt', 'w') as f:
    f.write(content)

