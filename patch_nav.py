import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

replacement = """@Composable
fun AppBottomNav(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val infiniteTransition = rememberInfiniteTransition(label = "glow_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_angle"
    )

    val sweepBrush = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            Color.Black.copy(alpha = 0.1f),
            Color.Black.copy(alpha = 0.9f),
            Color.Black.copy(alpha = 0.1f),
            Color.Transparent,
            Color.Transparent
        )
    )

    Box(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .drawBehind {
                rotate(angle) {
                    drawCircle(
                        brush = sweepBrush,
                        radius = size.width,
                        center = center
                    )
                }
            }
            .padding(2.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {"""

# Replace from @Composable fun AppBottomNav to NavigationBar {
pattern = r'@Composable\s*fun AppBottomNav.*?NavigationBar\([^)]*\)\s*\{'
new_content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(new_content)

print("Replaced!")
