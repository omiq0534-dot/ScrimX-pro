with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if '@Composable' in line and lines[i+1].startswith('fun DataRechargeCard'):
        start_idx = i
    if start_idx != -1 and line.startswith('fun JioBrandLogo'):
        # It's before JioBrandLogo
        # But we need to step back to find the actual end of DataRechargeCard
        for j in range(i-1, start_idx, -1):
            if lines[j].strip() == '}':
                end_idx = j
                break
        break

if start_idx != -1 and end_idx != -1:
    new_card = """@Composable
fun DataRechargeCard(
    plan: DataRechargePlan,
    userCoins: Int,
    onRedeemClick: () -> Unit
) {
    val canAfford = userCoins >= plan.coinPrice
    val isJio = plan.operator.equals("JIO", ignoreCase = true)
    
    val bgColor = if (isJio && plan.priceRupees == 349) androidx.compose.ui.graphics.Color(0xFFFFF6E5) else androidx.compose.ui.graphics.Color(0xFFF3F4F6)
    val bannerColor = if (isJio && plan.priceRupees == 349) androidx.compose.ui.graphics.Color(0xFFE58B29) else androidx.compose.ui.graphics.Color(0xFFD1E8F8)
    val bannerTextColor = if (isJio && plan.priceRupees == 349) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFF0F4770)
    val buyBtnColor = if (isJio && plan.priceRupees == 349) androidx.compose.ui.graphics.Color(0xFFE58B29) else androidx.compose.ui.graphics.Color(0xFF1D4ED8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, androidx.compose.ui.graphics.Color.LightGray, RoundedCornerShape(16.dp))
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
                        color = androidx.compose.ui.graphics.Color(0xFFB91C1C),
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
                    color = androidx.compose.ui.graphics.Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )
                
                Column {
                    Text("Validity", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
                    Text(plan.validity, color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                Column {
                    Text("Data", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 12.sp)
                    Text(plan.dataAmount, color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = buyBtnColor, modifier = Modifier.size(20.dp))
            }
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.5f))

            // Bottom Section (OTT & Buy)
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${plan.coinPrice} Coins",
                    color = androidx.compose.ui.graphics.Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Button(
                    onClick = onRedeemClick,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buyBtnColor,
                        disabledContainerColor = androidx.compose.ui.graphics.Color.Gray
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Buy", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
    print("Replaced!")
else:
    print(f"Failed. Start: {start_idx}, End: {end_idx}")

