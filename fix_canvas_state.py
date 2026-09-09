with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Fix ScrimXCrosshairs
old_crosshairs = """@Composable
fun ScrimXCrosshairs(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {"""

new_crosshairs = """@Composable
fun ScrimXCrosshairs(modifier: Modifier = Modifier) {
    val textPrimary = AppColors.TextPrimary
    Canvas(modifier = modifier) {"""
content = content.replace(old_crosshairs, new_crosshairs)

content = content.replace("color = AppColors.TextPrimary,", "color = textPrimary,")
content = content.replace("color = AppColors.TextPrimary)", "color = textPrimary)")

# Fix ScrimCard
old_scrim_card_box = """    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.CardBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {"""

new_scrim_card_box = """    val textPrimary = AppColors.TextPrimary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.CardBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {"""
content = content.replace(old_scrim_card_box, new_scrim_card_box)

content = content.replace("color = AppColors.TextPrimary.copy", "color = textPrimary.copy")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
