with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if 'fun TournamentDiscountCard(' in line:
        start_idx = i - 1 # Include @Composable
        break

if start_idx != -1:
    for i in range(start_idx + 2, len(lines)):
        if 'fun GooglePlayLogoIcon' in lines[i]:
            # Step back to find closing brace of TournamentDiscountCard
            for j in range(i-1, start_idx, -1):
                if lines[j].strip() == '}':
                    end_idx = j
                    break
            break

if start_idx != -1 and end_idx != -1:
    print(f"Found TournamentDiscountCard from {start_idx} to {end_idx}")
    
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
    
    val (gradientColors, textColor, buttonColor, iconColor) = when (item.cardColorTheme) {
        "GOLD" -> listOf(
            listOf(Color(0xFFFFF9E6), Color(0xFFFBE49D), Color(0xFFE5B935), Color(0xFFC0931B)), // Gradient
            Color(0xFF452B05), // Text
            Color(0xFF2C1A02), // Button
            Color(0xFFE5B935)  // Icon accent
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(metallicBrush)
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(enabled = canAfford, onClick = onBuyClick)
    ) {
        // Shine overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(200f, -100f),
                        radius = 800f
                    )
                )
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
}
"""
    new_lines = [line + '\n' for line in new_card.split('\n')]
    lines = lines[:start_idx] + new_lines + lines[end_idx+1:]
    
    with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
        f.writelines(lines)
    print("Replaced TournamentDiscountCard!")
else:
    print(f"Failed. Start: {start_idx}, End: {end_idx}")

