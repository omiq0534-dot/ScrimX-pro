import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Replace DataRechargeDialog
new_dialog = """@Composable
fun DataRechargeDialog(
    plan: DataRechargePlan,
    userCoins: Int,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var mobileNumber by remember { mutableStateOf("") }
    val isJio = plan.operator.equals("JIO", ignoreCase = true)
    val isValid = mobileNumber.length == 10 && mobileNumber[0] in listOf('6', '7', '8', '9')
    val canAfford = userCoins >= plan.coinPrice
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header with Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isJio) JioBrandLogo(size = 32) else AirtelBrandLogo(size = 28)
                        Column {
                            Text(
                                text = "${plan.operator} Data Recharge",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "₹${plan.priceRupees} Pack • ${plan.dataAmount}",
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !isLoading) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
                HorizontalDivider(color = Color.LightGray)
                // Pack Details Highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "⚡ Selected: ${plan.dataAmount} high-speed data",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Validity: ${plan.validity} • Official Price: ₹${plan.priceRupees}",
                            color = Color.DarkGray,
                            fontSize = 10.sp
                        )
                    }
                }
                // Mobile Number Input Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ENTER 10-DIGIT ${plan.operator} NUMBER",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 10) mobileNumber = filtered
                        },
                        placeholder = { Text("e.g. 9876543210", color = Color.Gray, fontSize = 13.sp) },
                        prefix = {
                            Text("+91 ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    )
                    if (mobileNumber.isNotEmpty() && !isValid) {
                        Text("Enter a valid 10-digit mobile number", color = Color.Red, fontSize = 10.sp)
                    } else if (isValid) {
                        Text("✅ Valid Indian mobile number", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // Coin Deduction Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF9FAFB))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recharge Cost:", color = Color.Gray, fontSize = 11.sp)
                            Text("${plan.coinPrice} Coins", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Your Coins:", color = Color.Gray, fontSize = 11.sp)
                            Text("$userCoins Coins", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("After Recharge:", color = Color.Gray, fontSize = 11.sp)
                            Text("${userCoins - plan.coinPrice} Coins", color = Color(0xFF16A34A), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
                // Strict Notice Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️ ", fontSize = 10.sp)
                            Text(
                                "महत्वपूर्ण सूचना / Notice:",
                                color = Color.Red,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "अगर मोबाइल नंबर गलत हुआ तो यूजर की खुद की जिम्मेदारी होगी।",
                            color = Color.DarkGray,
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onConfirm(mobileNumber) },
                        enabled = isValid && canAfford && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).height(42.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = if (isValid && canAfford) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RECHARGE NOW", color = if (isValid && canAfford) Color.White else Color.Gray, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}"""

# regex to replace DataRechargeDialog
content = re.sub(
    r'@Composable\s*fun DataRechargeDialog\s*\(.*?^}\s*}(?=\n\n|\Z)',
    new_dialog,
    content,
    flags=re.MULTILINE | re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

