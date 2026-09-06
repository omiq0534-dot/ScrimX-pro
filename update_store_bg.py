import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Update Scaffold background
content = content.replace('containerColor = Color(0xFF0B0C10),', 'containerColor = Color(0xFFF7F7FA),')

# Update TopAppBar
old_top_bar = """            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Store & Inventory",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            "Redeem Coins for Rewards",
                            color = Color(0xFF8E93A6),
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Coins Display
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF1E2130))
                            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$userCoins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0C10)
                )
            )"""

new_top_bar = """            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Store & Inventory",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            "Redeem Coins for Rewards",
                            color = Color(0xFF8E93A6),
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    // Coins Display
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$userCoins", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )"""

content = content.replace(old_top_bar, new_top_bar)

# Filter Chips
old_filter_bg = "} else Color(0xFF131622)"
new_filter_bg = "} else Color.White"
content = content.replace(old_filter_bg, new_filter_bg)

old_filter_border = "if (isSelected) Color.Transparent else Color(0xFF262A3C)"
new_filter_border = "if (isSelected) Color.Transparent else Color(0xFFE5E7EB)"
content = content.replace(old_filter_border, new_filter_border)

# Tab Text Colors
old_tab_row = """                    listOf("Data Packs", "Tickets", "Google Play", "History").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    color = if (selectedTab == index) Color(0xFFFFD700) else Color(0xFF6A7081),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }"""

new_tab_row = """                    listOf("Data Packs", "Tickets", "Google Play", "History").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    color = if (selectedTab == index) Color(0xFFFFD700) else Color(0xFF6A7081),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }"""

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
