import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

new_card = """@Composable
fun TournamentDiscountCard(
    item: StoreItem,
    effectivePrice: Int,
    isEnabled: Boolean,
    dailyLimit: Int,
    customCodesCount: Int,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit
) {
    val canAfford = userCoins >= effectivePrice && isEnabled
    
    // Premium Metallic Theme Definitions
    val (gradientColors, textColor, buttonColor, iconColor) = when (item.cardColorTheme) {
        "GOLD" -> listOf(
            listOf(Color(0xFFFDFBF7), Color(0xFFF6E8B6), Color(0xFFD4AF37), Color(0xFFAA7C11)), // Gradient
            Color(0xFF452B05), // Text
            Color(0xFF2C1A02), // Button
            Color(0xFFD4AF37)  // Icon accent
        )
        "DIAMOND" -> listOf(
            listOf(Color(0xFFF0FBFF), Color(0xFFD8F2FB), Color(0xFFA1E3F9), Color(0xFF4AC4E9)),
            Color(0xFF0C3852),
            Color(0xFF062336),
            Color(0xFF4AC4E9)
        )
        else -> listOf( // SILVER
            listOf(Color(0xFFF8F9FA), Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8)),
            Color(0xFF1E293B),
            Color(0xFF0F172A),
            Color(0xFF94A3B8)
        )
    }

    val metallicBrush = Brush.linearGradient(
        colors = gradientColors as List<Color>,
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    val shineBrush = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
        center = Offset(200f, -100f),
        radius = 800f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(metallicBrush)
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(enabled = canAfford, onClick = onBuyClick)
    ) {
        // Shine overlay for polished metal look
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shineBrush)
        )

        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background((textColor as Color).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "VIP PASS",
                        color = textColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                Icon(
                    Icons.Default.LocalPlay, 
                    contentDescription = null, 
                    tint = textColor.copy(alpha = 0.7f), 
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title & Description
            Text(
                text = item.title.uppercase(),
                color = textColor,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.subtitle,
                color = textColor.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.description,
                color = textColor.copy(alpha = 0.75f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = textColor.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Footer (Price & Buy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRICE",
                        color = textColor.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MonetizationOn, 
                            contentDescription = null, 
                            tint = textColor, 
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = effectivePrice.toString(),
                            color = textColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }

                Button(
                    onClick = onBuyClick,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor as Color,
                        disabledContainerColor = buttonColor.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Text(
                        "GET TICKET", 
                        color = if (canAfford) Color.White else Color.White.copy(alpha = 0.5f), 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}"""

content = re.sub(
    r'@Composable\s*fun TournamentDiscountCard.*?^}\s*}(?=\n\n@Composable|\Z)',
    new_card.strip(),
    content,
    flags=re.MULTILINE | re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
