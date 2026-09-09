import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# I will just replace the entire AppBottomNav function with the original one!
app_bottom_nav_original = """
@Composable
fun AppBottomNav(navController: NavController) {
    val navBackStackEntry by androidx.navigation.compose.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "glow_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(4000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "glow_angle"
    )

    val sweepBrush = androidx.compose.ui.graphics.Brush.sweepGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f),
            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f)
        )
    )

    Box(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .drawBehind {
                androidx.compose.ui.graphics.drawscope.rotate(angle) {
                    drawCircle(
                        brush = sweepBrush,
                        radius = size.width,
                        center = center
                    )
                }
            }
            .padding(2.dp) // border thickness
            .clip(RoundedCornerShape(26.dp))
            .background(androidx.compose.ui.graphics.Color.White)
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = androidx.compose.ui.graphics.Color.Black,
            tonalElevation = 0.dp
        ) {
            // Home
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == "home_tab" } == true,
                onClick = {
                    navController.navigate("home_tab") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home", fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                    selectedTextColor = androidx.compose.ui.graphics.Color.Black,
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
                )
            )

            // Matches
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == "matches_tab" } == true,
                onClick = {
                    navController.navigate("matches_tab") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Matches") },
                label = { Text("Matches", fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                    selectedTextColor = androidx.compose.ui.graphics.Color.Black,
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
                )
            )

            // Wallet
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == "wallet_tab" } == true,
                onClick = {
                    navController.navigate("wallet_tab") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                label = { Text("Wallet", fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                    selectedTextColor = androidx.compose.ui.graphics.Color.Black,
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
                )
            )

            // Profile
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == "profile_tab" } == true,
                onClick = {
                    navController.navigate("profile_tab") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("Profile", fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.ui.graphics.Color.Black,
                    selectedTextColor = androidx.compose.ui.graphics.Color.Black,
                    indicatorColor = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray
                )
            )
        }
    }
}
"""

# Replace the existing AppBottomNav with the original one
# We need to find the start of AppBottomNav and replace everything until the end of the file
start_index = content.find("@Composable\nfun AppBottomNav")
if start_index != -1:
    content = content[:start_index] + app_bottom_nav_original

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
