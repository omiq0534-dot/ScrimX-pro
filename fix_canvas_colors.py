import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Canvas draw blocks cannot resolve Composable properties directly, need to extract them first.
# Wait, AppColors.TextPrimary is a Composable! So it cannot be used inside drawCircle / Canvas { } block directly without assigning it to a variable first outside the block.

# 1. ScrimXCrosshairs
old_scrimx = """@Composable
fun ScrimXCrosshairs(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
"""
new_scrimx = """@Composable
fun ScrimXCrosshairs(modifier: Modifier = Modifier) {
    val textPrimary = AppColors.TextPrimary
    Canvas(modifier = modifier) {
"""
content = content.replace(old_scrimx, new_scrimx)
content = content.replace(
    'drawCircle(\n            color = AppColors.TextPrimary,',
    'drawCircle(\n            color = textPrimary,'
)
content = content.replace(
    'drawPath(needlePath, color = AppColors.TextPrimary)',
    'drawPath(needlePath, color = textPrimary)'
)

# 2. ScrimCard (Assuming there is a Canvas block here too)
# Let's find `Canvas(modifier = Modifier.matchParentSize()) {` in ScrimCard
old_scrim_card_canvas = """    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.CardBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {"""
        
new_scrim_card_canvas = """    val textPrimary = AppColors.TextPrimary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.CardBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {"""
content = content.replace(old_scrim_card_canvas, new_scrim_card_canvas)
content = content.replace(
    'color = AppColors.TextPrimary.copy(alpha = 0.04f)',
    'color = textPrimary.copy(alpha = 0.04f)'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

print("Canvas colors fixed.")
