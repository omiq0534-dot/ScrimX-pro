import re

with open('app/src/main/java/com/example/ui/screens/AdminStoreCodesScreen.kt', 'r') as f:
    content = f.read()

# Add RestockRequestCard composable
new_card = """
@Composable
fun RestockRequestCard(request: RestockRequest, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E070B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.itemTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(request.userEmail, color = Color(0xFFB0BEC5), fontSize = 11.sp)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFFE11D48))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStoreCodesScreen(navController: NavController) {"""
content = content.replace('@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun AdminStoreCodesScreen(navController: NavController) {', new_card)

# Insert the list in the LazyColumn
old_gp_text = """            item {
                Text(
                    "GOOGLE PLAY GIFT CARDS","""

new_gp_text = """            if (restockRequests.isNotEmpty()) {
                item {
                    Text(
                        "RESTOCK REQUESTS",
                        color = Color(0xFFE11D48),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp,
                        letterSpacing = 1.2.sp
                    )
                }
                items(restockRequests) { req ->
                    RestockRequestCard(
                        request = req,
                        onDismiss = {
                            FirebaseHelper.getFirestore()?.collection("store_restock_requests")?.document(req.id)?.update("status", "DISMISSED")
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            item {
                Text(
                    "GOOGLE PLAY GIFT CARDS","""
content = content.replace(old_gp_text, new_gp_text)

# Also fix the text saying "or auto-generates formatted codes!" since we removed auto-generation for Google Play
old_text = "from your pool or auto-generates formatted codes!"
new_text = "from your pool!"
content = content.replace(old_text, new_text)

with open('app/src/main/java/com/example/ui/screens/AdminStoreCodesScreen.kt', 'w') as f:
    f.write(content)
