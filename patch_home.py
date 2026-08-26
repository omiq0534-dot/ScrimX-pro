import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# 1. Update HomeScreen parameters and state to pass name and handle Watch dialog
pattern_home = r'@Composable\s*fun HomeScreen.*?LazyColumn\('
replacement_home = """@Composable
fun HomeScreen(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    val profile by userViewModel.profile.collectAsState()
    var showWatchDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    if (showWatchDialog) {
        AlertDialog(
            onDismissRequest = { showWatchDialog = false },
            title = { Text("Watch & Earn", fontWeight = FontWeight.Bold) },
            text = { Text("Choose what you want to watch.") },
            confirmButton = {
                Button(
                    onClick = { showWatchDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Watch Ad (Earn)", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showWatchDialog = false },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Watch Scrims Live (YT)")
                }
            }
        )
    }

    LazyColumn("""
content = re.sub(pattern_home, replacement_home, content, flags=re.DOTALL)


# 2. Update TopWalletBar call
content = content.replace("TopWalletBar(navController, profile?.appMoney ?: 0, profile?.realMoney ?: 0)",
                          "TopWalletBar(navController, profile?.appMoney ?: 0, profile?.realMoney ?: 0, profile?.name ?: \"User\")")

# 3. Update EarningZone call
content = content.replace("item { EarningZone() }", "item { EarningZone(onWatchClick = { showWatchDialog = true }) }")

# 4. Update TopWalletBar definition
pattern_top_bar = r'@Composable\s*fun TopWalletBar\(navController: NavController\? = null, appMoney: Int = 0, realMoney: Int = 0\).*?Icon\(Icons\.Default\.Logout, contentDescription = "Logout", tint = Color\.White, modifier = Modifier\.size\(24\.dp\)\)\s*\}'
replacement_top_bar = """@Composable
fun TopWalletBar(navController: NavController? = null, appMoney: Int = 0, realMoney: Int = 0, userName: String = "User") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Pic Placeholder (Click to profile)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable {
                    // Navigate to profile_tab using standard bottom nav approach or just use root
                    navController?.navigate("profile_tab") {
                        popUpTo("home_tab") { inclusive = false }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "U"
            Text(initial, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
        }"""
content = re.sub(pattern_top_bar, replacement_top_bar, content, flags=re.DOTALL)

# 5. Update EarningZone definition
pattern_earning = r'@Composable\s*fun EarningZone\(\).*?EarnCard\("Watch", Icons\.Default\.PlayArrow, Modifier\.weight\(1f\)\)'
replacement_earning = """@Composable
fun EarningZone(onWatchClick: () -> Unit = {}) {
    Column {
        Text("Earning Zone", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            EarnCard("Daily", Icons.Default.CardGiftcard, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            EarnCard("Spin", Icons.Default.Star, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            EarnCard("Watch", Icons.Default.PlayArrow, Modifier.weight(1f), onClick = onWatchClick)"""
content = re.sub(pattern_earning, replacement_earning, content, flags=re.DOTALL)

# 6. Update EarnCard
pattern_earn_card = r'fun EarnCard\(title: String, icon: androidx\.compose\.ui\.graphics\.vector\.ImageVector, modifier: Modifier = Modifier\) \{\s*Column\(\s*modifier = modifier'
replacement_earn_card = """fun EarnCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }"""
content = re.sub(pattern_earn_card, replacement_earn_card, content, flags=re.DOTALL)

# 7. Update MatchCard Live button
pattern_match_btn = r'Button\(\s*onClick = onClick,\s*colors = ButtonDefaults\.buttonColors\(containerColor = Color\.Black, contentColor = Color\.White\),\s*shape = CircleShape,\s*contentPadding = PaddingValues\(horizontal = 24\.dp, vertical = 14\.dp\)\s*\)\s*\{\s*Text\("Join • \$entry", fontWeight = FontWeight\.Bold, fontSize = 14\.sp\)\s*\}'
replacement_match_btn = """val isLive = status == "Live" || status == "Ongoing"
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLive) Color.Red else Color.Black, 
                    contentColor = Color.White
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            ) {
                if (isLive) {
                    Text("🔴 WATCH LIVE", fontWeight = FontWeight.Black, fontSize = 14.sp)
                } else {
                    Text("Join • $entry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }"""
content = re.sub(pattern_match_btn, replacement_match_btn, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

print("HomeScreen patched successfully!")
