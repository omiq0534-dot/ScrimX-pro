import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Fix CelebrationCardDialog
content = re.sub(
    r'@Composable\s*fun CelebrationCardDialog\(onDismiss: \(\) -> Unit\) \{.*?\}(?=\n\n@Composable\s*fun TournamentDiscountCard)',
    """@Composable
fun CelebrationCardDialog(
    card: UserPurchasedCard,
    userName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF14161F))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉 SUCCESS!", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("You bought ${card.title}", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))) {
                    Text("AWESOME", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}""",
    content, flags=re.DOTALL
)

# Fix TournamentDiscountCard
content = re.sub(
    r'@Composable\s*fun TournamentDiscountCard\(\s*item: StoreItem,\s*userCoins: Int,\s*onBuyClick: \(\) -> Unit\s*\)\s*\{.*?(?=\n@Composable\s*fun GooglePlayLogoIcon)',
    """@Composable
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(item.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(item.subtitle, color = Color(0xFF6F8299), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBuyClick,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BUY FOR ${effectivePrice} COINS", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}
""",
    content, flags=re.DOTALL
)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

