import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Define the new FamPayGooglePlayCard component
new_card = """@Composable
fun FamPayGooglePlayCard(
    item: StoreItem,
    effectivePrice: Int = item.coinPrice,
    isEnabled: Boolean = true,
    dailyLimit: Int = 2,
    customCodesCount: Int = 0,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit
) {
    val canAfford = userCoins >= effectivePrice && isEnabled
    var isBought by remember { mutableStateOf(false) }

    // Continuous Shimmer / Sheen Animation
    val infiniteTransition = rememberInfiniteTransition(label = "googlePlayShimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0C0E17))
            .border(1.dp, if (isBought) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFF1E2338), RoundedCornerShape(22.dp))
            .padding(12.dp)
    ) {
        // Physical Diamond Glass Gift Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF061520),
                            Color(0xFF0B2433),
                            Color(0xFF04101A),
                            Color(0xFF081C29)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f)
                    )
                )
                .border(
                    0.8.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.5f),
                            Color(0xFF00E676).copy(alpha = 0.5f),
                            Color(0xFF00B0FF).copy(alpha = 0.4f),
                            Color(0xFFFFD700).copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(800f, 600f)
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            // Diamond Glass Facets & Moving Shimmer Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Subtle diamond crystalline geometry
                val facetPath = Path().apply {
                    moveTo(w * 0.7f, 0f)
                    lineTo(w, h * 0.45f)
                    lineTo(w * 0.5f, h)
                    lineTo(0f, h * 0.35f)
                    close()
                }
                drawPath(
                    path = facetPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.05f), Color.Transparent),
                        start = Offset(w * 0.7f, 0f),
                        end = Offset(0f, h)
                    )
                )

                // Dynamic Moving Shimmer Sheen Beam
                val sheenWidth = 140f
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.12f),
                        Color(0xFF00E5FF).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - sheenWidth, 0f),
                    end = Offset(shimmerTranslate + sheenWidth, h)
                )
                drawRect(brush = sheenBrush)

                // Sparkling Diamond Stars (✦)
                drawCircle(Color(0xFF00E5FF).copy(alpha = 0.7f), radius = 2.dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 1.2.dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                drawCircle(Color(0xFF69F0AE).copy(alpha = 0.6f), radius = 1.5.dp.toPx(), center = Offset(w * 0.92f, h * 0.38f))
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Authentic Google Play Logo + Denomination Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google Play Brand Logo & Text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF020D14).copy(alpha = 0.7f))
                            .border(0.8.dp, Color(0xFF00E5FF).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        GooglePlayLogoIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(7.dp))
                        Column {
                            Text(
                                "Google Play",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.5.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "DIGITAL GIFT VOUCHER",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 7.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Diamond Glass Denomination Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00E5FF), Color(0xFF00E676))
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "₹${item.denominationRupees}",
                            color = Color(0xFF02131D),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                // Middle Row: Buy-To-Reveal Code Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF030914).copy(alpha = 0.85f))
                        .border(1.dp, if (isBought) Color(0xFF06B6D4) else Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Crossfade(targetState = isBought, animationSpec = tween(durationMillis = 600), label = "Reveal") { revealed ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!revealed) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Protected Code",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "GP •••• 8K9F ••••",
                                        color = Color(0xFFE0F7FA),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.2.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "16-CHAR CODE",
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.5.sp
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Code",
                                        tint = Color(0xFF06B6D4),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "GPK3-9M2X-8K9F-2B4L",
                                        color = Color(0xFF06B6D4),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Row: Gamer Name & Limit / In-Stock Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "CLAIMANT",
                            color = Color(0xFF6F8299),
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.5.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            userName.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF14161F))
                            .border(0.5.dp, Color(0xFF262A38), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (customCodesCount > 0) "IN STOCK" else "LIMIT $dailyLimit/DAY",
                            color = if (customCodesCount > 0) Color(0xFF00E676) else Color(0xFF6F8299),
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Store Purchase Interaction
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(targetValue = if (isPressed && !isBought) 0.95f else 1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f))

        Button(
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
        }
    }
}
"""

match = re.search(r'@Composable\s*fun FamPayGooglePlayCard.*?(?=\n@Composable\s*fun GooglePlayLogoIcon)', content, flags=re.DOTALL)
if match:
    content = content[:match.start()] + new_card + content[match.end():]
    with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
        f.write(content)
    print("Successfully replaced FamPayGooglePlayCard")
else:
    print("Could not find FamPayGooglePlayCard")

