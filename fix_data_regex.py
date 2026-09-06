import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

new_data_card = """@Composable
fun DataRechargeCard(
    plan: DataRechargePlan,
    userCoins: Int,
    onRedeemClick: () -> Unit
) {
    val canAfford = userCoins >= plan.coinPrice
    val isJio = plan.operator.equals("JIO", ignoreCase = true)
    
    val bgColor = if (isJio && plan.priceRupees == 349) Color(0xFFFFF6E5) else Color(0xFFF3F4F6)
    val bannerColor = if (isJio && plan.priceRupees == 349) Color(0xFFE58B29) else Color(0xFFD1E8F8)
    val bannerTextColor = if (isJio && plan.priceRupees == 349) Color.White else Color(0xFF0F4770)
    val buyBtnColor = if (isJio && plan.priceRupees == 349) Color(0xFFE58B29) else Color(0xFF1D4ED8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onRedeemClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Section (Banner & 5G)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top-Left Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomEnd = 8.dp))
                        .background(bannerColor)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = plan.tagText,
                        color = bannerTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                
                // TRUE 5G
                if (plan.highlightSpeed.contains("5G")) {
                    Text(
                        text = "TRUE 5G",
                        color = Color(0xFFB91C1C),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp, end = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price and Details
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${plan.priceRupees}",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )
                
                Column {
                    Text("Validity", color = Color.Gray, fontSize = 12.sp)
                    Text(plan.validity, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                Column {
                    Text("Data", color = Color.Gray, fontSize = 12.sp)
                    Text(plan.dataAmount, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = buyBtnColor, modifier = Modifier.size(20.dp))
            }
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

            // Bottom Section (OTT & Buy)
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${plan.coinPrice} Coins",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Button(
                    onClick = onRedeemClick,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buyBtnColor,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Buy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
"""

content = re.sub(
    r'@Composable\s*fun DataRechargeCard.*?^}\s*}(?=\n\n@Composable|\Z)',
    new_data_card.strip(),
    content,
    flags=re.MULTILINE | re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
