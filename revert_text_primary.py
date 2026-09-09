import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# I replaced `AppColors.TextPrimary` with `textPrimary` globally in HomeScreen in an earlier script. 
# Let's revert that and just use `AppColors.TextPrimary()` instead. 
# Oh wait, `AppColors.TextPrimary` is NOT a function, it's a property. 
# So `AppColors.TextPrimary` can be used ANYWHERE in Composable scope.
# BUT inside Canvas {} it's not composable scope. 

# Revert ALL textPrimary to AppColors.TextPrimary
content = content.replace("textPrimary", "AppColors.TextPrimary")

# Now selectively fix only the Canvas blocks by using a local variable outside Canvas
old_scrimx = """@Composable
fun ScrimXCrosshairs(modifier: Modifier = Modifier) {
    val AppColors.TextPrimary = AppColors.TextPrimary
    Canvas(modifier = modifier) {"""

new_scrimx = """@Composable
fun ScrimXCrosshairs(modifier: Modifier = Modifier) {
    val canvasColor = AppColors.TextPrimary
    Canvas(modifier = modifier) {"""
content = content.replace(old_scrimx, new_scrimx)

# Note: `AppColors.TextPrimary` is now everywhere. Let's fix the Canvas draws in ScrimXCrosshairs
content = content.replace(
    'drawCircle(\n            color = AppColors.TextPrimary,\n            radius = radius * 0.16f',
    'drawCircle(\n            color = canvasColor,\n            radius = radius * 0.16f'
)
content = content.replace(
    'drawPath(needlePath, color = AppColors.TextPrimary)',
    'drawPath(needlePath, color = canvasColor)'
)

old_scrim_card = """fun ScrimCard(
    title: String,
    subtitle: String,
    badgeText: String,
    prizePool: String,
    spotsLeft: String,
    progress: Float,
    imageAsset: Int? = null,
    onClick: () -> Unit,
    iconContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, animationSpec = tween(150))
    
    val baseGlowColor = Color(0xFF00E5FF)
    
    val AppColors.TextPrimary = AppColors.TextPrimary"""

new_scrim_card = """fun ScrimCard(
    title: String,
    subtitle: String,
    badgeText: String,
    prizePool: String,
    spotsLeft: String,
    progress: Float,
    imageAsset: Int? = null,
    onClick: () -> Unit,
    iconContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, animationSpec = tween(150))
    
    val baseGlowColor = Color(0xFF00E5FF)
    
    val canvasColor = AppColors.TextPrimary"""
content = content.replace(old_scrim_card, new_scrim_card)

# And in ScrimCard canvas:
content = content.replace(
    'color = AppColors.TextPrimary.copy(alpha = 0.04f),',
    'color = canvasColor.copy(alpha = 0.04f),'
)

# And we have one more place: "val textPrimary" was injected randomly. Let's just remove `val AppColors.TextPrimary = AppColors.TextPrimary` entirely
content = content.replace("val AppColors.TextPrimary = AppColors.TextPrimary\n", "")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
print("Canvas logic fixed.")
