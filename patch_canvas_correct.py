import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# I thought ScrimXCrosshairs was in HomeScreen but it's not! SpinWheelIcon is!
old_spin_wheel = """fun SpinWheelIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {"""

new_spin_wheel = """fun SpinWheelIcon(modifier: Modifier = Modifier) {
    val canvasColor = AppColors.TextPrimary
    Canvas(modifier = modifier) {"""
content = content.replace(old_spin_wheel, new_spin_wheel)


# In EarnCard (which I thought was ScrimCard):
# Let's find EarnCard's Canvas block.
old_earn_card_box = """    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.CardBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {"""

new_earn_card_box = """    val canvasColor = AppColors.TextPrimary
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
content = content.replace(old_earn_card_box, new_earn_card_box)

# Now, any remaining `AppColors.TextPrimary` inside Canvas blocks will fail.
# So we need to ensure ONLY outside canvas it uses AppColors.TextPrimary. 
# But wait, earlier I replaced `textPrimary` with `AppColors.TextPrimary` globally, which broke the canvas blocks.
# And inside those Canvas blocks, I already replaced `AppColors.TextPrimary` with `canvasColor`.
# Are there any other Canvas blocks in HomeScreen?
# Let's check!
