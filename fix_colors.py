import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Fix Out of stock button colors inside FamPayGooglePlayCard
content = content.replace(
    'if (isOutOfStock) Color(0xFF3B82F6).copy(alpha = 0.15f)',
    'if (isOutOfStock) Color(0xFF262626).copy(alpha = 0.15f)'
)
content = content.replace(
    'if (isOutOfStock) Color(0xFF3B82F6).copy(alpha = 0.5f)',
    'if (isOutOfStock) Color(0xFF262626).copy(alpha = 0.5f)'
)
content = content.replace(
    'tint = Color(0xFF3B82F6)',
    'tint = Color(0xFF262626)'
)
content = content.replace(
    'color = Color(0xFF3B82F6)',
    'color = Color(0xFF262626)'
)

# Fix Vault Card colors to be uniformly "cray black" (dark gray/black)
content = content.replace(
    'val cardColor = if (isUsed) Color.Gray else if (isDataRecharge) Color(0xFF3B82F6) else Color(0xFF06B6D4)',
    'val cardColor = if (isUsed) Color.Gray else Color(0xFF262626)'
)
# Make text color in vault button White if active
content = content.replace(
    'Text("MARK AS USED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)',
    'Text("MARK AS USED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)'
)


# Now fix Google Play Card Animation and overlay
old_card_box = """        // Physical Diamond Glass Gift Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0A2940),
                            Color(0xFF061520),
                            Color(0xFF02070A)
                        ),
                        center = Offset(400f, 200f),
                        radius = 800f
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                            Color(0xFF00E676).copy(alpha = pulseAlpha),
                            Color(0xFF00B0FF).copy(alpha = pulseAlpha),
                            Color(0xFF00E5FF).copy(alpha = pulseAlpha)
                        ),
                        start = Offset(shimmerTranslate - 500f, 0f),
                        end = Offset(shimmerTranslate + 500f, 600f)
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            // Diamond Glass Facets & Ultra-Premium Moving Shimmer Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Holographic crystalline geometry
                val facetPath = Path().apply {
                    moveTo(w * 0.6f, 0f)
                    lineTo(w, h * 0.4f)
                    lineTo(w * 0.4f, h)
                    lineTo(0f, h * 0.4f)
                    close()
                }
                drawPath(
                    path = facetPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.08f), Color.Transparent),
                        start = Offset(w * 0.6f, 0f),
                        end = Offset(0f, h)
                    )
                )

                // Base Slow Shimmer (Wide)
                val sheenWidth = 250f
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00E5FF).copy(alpha = 0.15f * pulseAlpha),
                        Color.White.copy(alpha = 0.2f * pulseAlpha),
                        Color(0xFF00E676).copy(alpha = 0.15f * pulseAlpha),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - sheenWidth, -sheenWidth),
                    end = Offset(shimmerTranslate + sheenWidth, h + sheenWidth)
                )
                drawRect(brush = sheenBrush)
                
                // Fast Sharp Holographic Flash
                val fastSheenWidth = 60f
                val fastSheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.8f),
                        Color.Transparent
                    ),
                    start = Offset(fastShimmer - fastSheenWidth, 0f),
                    end = Offset(fastShimmer + fastSheenWidth, h)
                )
                drawRect(brush = fastSheenBrush)

                // Sparkling Diamond Stars (✦) with animated pulse
                drawCircle(Color(0xFF00E5FF).copy(alpha = 0.7f * pulseAlpha), radius = (2.5f * pulseAlpha).dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                drawCircle(Color.White.copy(alpha = 0.9f * pulseAlpha), radius = (1.5f * pulseAlpha).dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                
                drawCircle(Color(0xFF69F0AE).copy(alpha = 0.6f * pulseAlpha), radius = (2f * pulseAlpha).dp.toPx(), center = Offset(w * 0.92f, h * 0.38f))
                drawCircle(Color.White, radius = 1.dp.toPx(), center = Offset(w * 0.92f, h * 0.38f))
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {"""

# We need to split the Canvas. Crystals in background. Column content. Then Shimmer and stars overlay.
# Also fix padding. We should apply padding TO THE COLUMN, not the box, so the canvas draws edge-to-edge!
new_card_box = """        // Physical Diamond Glass Gift Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0A2940),
                            Color(0xFF061520),
                            Color(0xFF02070A)
                        ),
                        center = Offset(400f, 200f),
                        radius = 800f
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                            Color(0xFF00E676).copy(alpha = pulseAlpha),
                            Color(0xFF00B0FF).copy(alpha = pulseAlpha),
                            Color(0xFF00E5FF).copy(alpha = pulseAlpha)
                        ),
                        start = Offset(shimmerTranslate - 500f, 0f),
                        end = Offset(shimmerTranslate + 500f, 600f)
                    ),
                    RoundedCornerShape(18.dp)
                )
        ) {
            // Background Canvas (Crystals)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Holographic crystalline geometry 1
                val facetPath1 = Path().apply {
                    moveTo(w * 0.6f, 0f)
                    lineTo(w, h * 0.4f)
                    lineTo(w * 0.4f, h)
                    lineTo(0f, h * 0.4f)
                    close()
                }
                drawPath(
                    path = facetPath1,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(w * 0.6f, 0f),
                        end = Offset(0f, h)
                    )
                )

                // Holographic crystalline geometry 2
                val facetPath2 = Path().apply {
                    moveTo(w * 0.2f, h)
                    lineTo(w * 0.8f, h * 0.3f)
                    lineTo(w, h * 0.7f)
                    lineTo(w, h)
                    close()
                }
                drawPath(
                    path = facetPath2,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E676).copy(alpha = 0.12f), Color.Transparent),
                        start = Offset(w * 0.8f, h),
                        end = Offset(w * 0.2f, 0f)
                    )
                )
            }

            // Content inside the card
            Column(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {"""

# Replace old_card_box
content = content.replace(old_card_box, new_card_box)

# Now we need to add the overlay Canvas at the end of the Box.
# Find the end of the Column inside the Box.
# The Column ends right before the Out of Stock / interaction section.
overlay_target = """                // Bottom Row: Gamer Name & Limit / In-Stock Badge
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
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF3F4F6))
                            .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (customCodesCount > 0) "IN STOCK" else "LIMIT $dailyLimit/DAY",
                            color = if (customCodesCount > 0) Color(0xFF22C55E) else Color.Gray,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))"""

overlay_replacement = """                // Bottom Row: Gamer Name & Limit / In-Stock Badge
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
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF3F4F6))
                            .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (customCodesCount > 0) "IN STOCK" else "LIMIT $dailyLimit/DAY",
                            color = if (customCodesCount > 0) Color(0xFF22C55E) else Color.Gray,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Foreground Canvas (Shimmer & Stars Overlay)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Base Slow Shimmer (Wide) covering entire card
                val sheenWidth = 250f
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00E5FF).copy(alpha = 0.25f * pulseAlpha),
                        Color.White.copy(alpha = 0.4f * pulseAlpha),
                        Color(0xFF00E676).copy(alpha = 0.25f * pulseAlpha),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - sheenWidth, -sheenWidth),
                    end = Offset(shimmerTranslate + sheenWidth, h + sheenWidth)
                )
                // Draw over the entire card with BlendMode.Screen to act purely as light
                drawRect(brush = sheenBrush, blendMode = BlendMode.Screen)
                
                // Fast Sharp Holographic Flash
                val fastSheenWidth = 60f
                val fastSheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.8f),
                        Color.Transparent
                    ),
                    start = Offset(fastShimmer - fastSheenWidth, 0f),
                    end = Offset(fastShimmer + fastSheenWidth, h)
                )
                drawRect(brush = fastSheenBrush, blendMode = BlendMode.Screen)

                // Sparkling Diamond Stars (✦) with animated pulse drawn on top
                drawCircle(Color(0xFF00E5FF).copy(alpha = 0.8f * pulseAlpha), radius = (3.5f * pulseAlpha).dp.toPx(), center = Offset(w * 0.85f, h * 0.15f))
                drawCircle(Color.White.copy(alpha = 0.9f * pulseAlpha), radius = (2f * pulseAlpha).dp.toPx(), center = Offset(w * 0.85f, h * 0.15f))
                
                drawCircle(Color(0xFF69F0AE).copy(alpha = 0.8f * pulseAlpha), radius = (3f * pulseAlpha).dp.toPx(), center = Offset(w * 0.92f, h * 0.45f))
                drawCircle(Color.White, radius = 1.5.dp.toPx(), center = Offset(w * 0.92f, h * 0.45f))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))"""

content = content.replace(overlay_target, overlay_replacement)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

