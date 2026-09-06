import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# 1. Main Tabs (Line ~334)
# .background(if (isSelected) Color(0xFFFFD700) else Color.Transparent)
# We will use white for selected, and dark grey background is already there.
content = re.sub(
    r'\.background\(if \(isSelected\) Color\(0xFFFFD700\) else Color\.Transparent\)',
    r'.background(if (isSelected) Color(0xFF262626) else Color.Transparent)',
    content
)
# For the icon and text in tabs, when selected we want them white instead of black.
content = re.sub(
    r'tint = if \(isSelected\) Color\.Black else Color\(0xFF8E93A6\)',
    r'tint = if (isSelected) Color.White else Color(0xFF8E93A6)',
    content
)
content = re.sub(
    r'color = if \(isSelected\) Color\.Black else Color\(0xFF8E93A6\)',
    r'color = if (isSelected) Color.White else Color(0xFF8E93A6)',
    content
)

# 2. Operator Filter Chips (Line ~399)
#                                                     if (isSelected) {
#                                                         when (op) {
#                                                             "JIO" -> Color(0xFFD32F2F) // Vibrant Crimson
#                                                             "AIRTEL" -> Color(0xFFE53935)
#                                                             else -> Color(0xFFFFC107) // Neon Amber/Yellow
#                                                         }
#                                                     } else Color.White
op_bg_regex = r'if \(isSelected\) \{\s*when \(op\) \{\s*"JIO" -> Color\(0xFFD32F2F\).*?"AIRTEL" -> Color\(0xFFE53935\).*?else -> Color\(0xFFFFC107\).*?\}\s*\} else Color\.White'
op_bg_replacement = 'if (isSelected) Color(0xFF141414) else Color.White'
content = re.sub(op_bg_regex, op_bg_replacement, content, flags=re.DOTALL)

#                                                 color = if (isSelected) (if (op == null) Color.Black else Color.White) else Color(0xFF8E93A6),
op_text_regex = r'color = if \(isSelected\) \(if \(op == null\) Color\.Black else Color\.White\) else Color\(0xFF8E93A6\)'
op_text_replacement = 'color = if (isSelected) Color.White else Color(0xFF8E93A6)'
content = re.sub(op_text_regex, op_text_replacement, content)

# 3. Cyan removal from cards
# We want to replace 0xFF00E5FF (Cyan/Diamond) with 0xFFE2E8F0 (Silver/Platinum) or White. Let's use 0xFFE2E8F0.
content = content.replace('Color(0xFF00E5FF)', 'Color(0xFFE2E8F0)')
# Remove cyan glow shadows from cards
content = content.replace('Color(0xFF00E5FF).copy(alpha = 0.2f)', 'Color.Black.copy(alpha = 0.5f)')
content = content.replace('Color(0xFF00E5FF).copy(alpha = 0.1f)', 'Color(0xFF262626)')

# 4. Rewrite PurchasedCardVaultItem completely to fix "tap to reveal" and blur.
vault_item_regex = r'@Composable\nfun PurchasedCardVaultItem\(.*?(?=\n@Composable|\n// ---|\Z)'

new_vault_item = """@Composable
fun PurchasedCardVaultItem(
    card: UserPurchasedCard,
    userName: String,
    onMarkUsed: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var isCodeCopied by remember { mutableStateOf(false) }
    var isRevealed by remember { mutableStateOf(false) }

    val isDataRecharge = card.category.equals("DATA_RECHARGE", ignoreCase = true)
    val isGooglePlay = card.category.equals("GOOGLE_PLAY", ignoreCase = true)
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
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141414)) // Hardcore Black Base
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
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
                    if (isUsed) "USED" else "ACTIVE",
                    color = if (isUsed) Color(0xFF444444) else Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        // Details and Blur Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isDataRecharge) {
                // Data Recharge Proof View
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
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262626))
                        .clickable(enabled = proofBitmap != null) {
                            if (proofBitmap != null) showProofDialog = true
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (proofBitmap != null) "VIEW RECEIPT" else "PROCESSING...",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                }
            } else {
                // Redeem Code / Gift Card View
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

                // Blur container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262626))
                        .clickable(enabled = card.code.isNotBlank()) {
                            if (!isRevealed && card.code.isNotBlank()) {
                                isRevealed = true
                            } else if (isRevealed && card.code.isNotBlank()) {
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("Code", card.code))
                                isCodeCopied = true
                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // We apply blur modifier to the text if it's not revealed
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
                    
                    // Overlay for Tap to Claim (only when blurred)
                    if (!isRevealed && card.code.isNotBlank()) {
                        Text(
                            "TAP TO CLAIM",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                if (isRevealed) {
                    Text(
                        text = if (isCodeCopied) "COPIED TO CLIPBOARD" else "TAP TO COPY CODE",
                        color = Color(0xFFA0AAB2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Mark Used Button
        if (!isUsed && (card.code.isNotBlank() || proofBitmap != null)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { onMarkUsed() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "MARK AS CLAIMED",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
"""

content = re.sub(vault_item_regex, new_vault_item, content, flags=re.MULTILINE | re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

