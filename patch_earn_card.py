import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Add val canvasColor = AppColors.TextPrimary to EarnCard and SpinWheelIcon
if "val canvasColor = AppColors.TextPrimary" not in content.split("fun SpinWheelIcon")[1].split("Canvas")[0]:
    content = content.replace("fun SpinWheelIcon(modifier: Modifier = Modifier) {\n    Canvas(modifier = modifier) {", "fun SpinWheelIcon(modifier: Modifier = Modifier) {\n    val canvasColor = AppColors.TextPrimary\n    Canvas(modifier = modifier) {")

if "val canvasColor = AppColors.TextPrimary" not in content.split("fun EarnCard")[1].split("Canvas")[0]:
    old = """    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.CardBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {"""
    new = """    val canvasColor = AppColors.TextPrimary
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
    content = content.replace(old, new)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

print("Double check complete.")
