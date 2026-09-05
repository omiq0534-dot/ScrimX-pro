import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Add Crossfade import
if 'import androidx.compose.animation.Crossfade' not in content:
    content = content.replace('import androidx.compose.animation.core.*', 'import androidx.compose.animation.core.*\nimport androidx.compose.animation.Crossfade')

# Restore the missing components
missing_components = """
@Composable
fun PurchasedCardVaultItem(
    card: UserPurchasedCard,
    userName: String,
    onMarkUsed: () -> Unit
) {
    val isUsed = card.status.equals("USED", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14161F))
            .border(1.dp, if (isUsed) Color(0xFF262A38) else Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(card.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(card.category, color = Color(0xFF6F8299), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            if (isUsed) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF262A38)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("USED", color = Color(0xFF6F8299), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF00E676).copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("ACTIVE", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C0E17))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                card.code.ifBlank { "PENDING ADMIN APPROVAL" },
                color = if (card.code.isNotBlank()) Color(0xFF00E5FF) else Color(0xFFFFD700),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
        
        if (card.code.isNotBlank() && !isUsed) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onMarkUsed,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Text("MARK AS USED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CelebrationCardDialog(onDismiss: () -> Unit) {
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
                Text("Item purchased successfully.", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))) {
                    Text("AWESOME", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun TournamentDiscountCard(
    item: StoreItem,
    userCoins: Int,
    onBuyClick: () -> Unit
) {
    val canAfford = userCoins >= item.coinPrice
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
            Text("BUY FOR ${item.coinPrice} COINS", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}
"""

content = content.replace('@Composable\nfun GooglePlayLogoIcon', missing_components + '\n@Composable\nfun GooglePlayLogoIcon')

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
