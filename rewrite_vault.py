import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

old_vault_regex = r'@Composable\s*fun PurchasedCardVaultItem\(.*?(?=\n@Composable|\n// ----------------|\Z)'
new_vault = """@Composable
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
                            Text("Official receipt from admin", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                    .border(1.dp, if (isUsed) Color(0xFF444444) else Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isUsed) "USED" else "ACTIVE",
                    color = if (isUsed) Color(0xFF444444) else Color(0xFF00E5FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        // Action Area (Code / Proof Reveal)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Using AnimatedContent for a slick reveal
            AnimatedContent(
                targetState = isRevealed || isUsed,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn(tween(400)))
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut(tween(400)))
                },
                label = "revealAnimation"
            ) { revealed ->
                if (revealed) {
                    // Code or Proof is Revealed
                    if (isDataRecharge) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().clickable(enabled = proofBitmap != null) {
                                if (proofBitmap != null) showProofDialog = true
                            }
                        ) {
                            Text(
                                if (proofBitmap != null) "VIEW RECEIPT" else "PROCESSING...",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (proofBitmap != null) "TAP TO OPEN" else "PLEASE WAIT",
                                color = Color(0xFFA0AAB2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    } else {
                        // Gift Card / VIP Code
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().clickable(enabled = card.code.isNotBlank()) {
                                if (card.code.isNotBlank()) {
                                    clipboardManager.setPrimaryClip(ClipData.newPlainText("Code", card.code))
                                    isCodeCopied = true
                                    Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text(
                                card.code.ifBlank { "PENDING..." },
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isCodeCopied) "COPIED TO CLIPBOARD" else "TAP TO COPY",
                                color = Color(0xFFA0AAB2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                } else {
                    // Hidden State - "TAP TO REVEAL"
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alphaPulse by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alphaPulse"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isRevealed = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Dark metallic plate
                            drawRect(color = Color(0xFF0A0A0A))
                            // Subtle scanlines
                            for (i in 0..10) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.05f),
                                    start = Offset(0f, i * 30f),
                                    end = Offset(size.width, i * 30f),
                                    strokeWidth = 2f
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = alphaPulse), RoundedCornerShape(8.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.1f))
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "TAP TO REVEAL",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 3.sp
                            )
                        }
                    }
                }
            }
        }

        // Mark Used Button
        if (!isUsed && (card.code.isNotBlank() || proofBitmap != null)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00E5FF))
                    .clickable { onMarkUsed() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "MARK AS CLAIMED",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}
"""
content = re.sub(old_vault_regex, new_vault, content, flags=re.MULTILINE | re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

