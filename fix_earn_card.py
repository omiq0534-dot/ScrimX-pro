import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# I see it's EarnCard! And `val canvasColor` was missing or broken. Let's fix EarnCard fully.
old_earn_card = """@Composable
fun EarnCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "ScaleAnim")
    
    val baseGlowColor = Color.White

    Box("""

new_earn_card = """@Composable
fun EarnCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "ScaleAnim")
    
    val baseGlowColor = AppColors.TextPrimary
    val canvasColor = AppColors.TextPrimary

    Box("""
content = content.replace(old_earn_card, new_earn_card)

# And `Color(0xFF14161F)` and `Color(0xFF262A38)`
content = content.replace(
    '.background(Color(0xFF14161F))',
    '.background(AppColors.CardBackground)'
)
content = content.replace(
    '.border(1.dp, Color(0xFF262A38), RoundedCornerShape(24.dp))',
    '.border(1.dp, AppColors.BorderColor, RoundedCornerShape(24.dp))'
)

# And `Color(0xFF1B1E2B)` inside EarnCard -> AppColors.SubCardBackground
content = content.replace(
    '.background(Color(0xFF1B1E2B))',
    '.background(AppColors.SubCardBackground)'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
