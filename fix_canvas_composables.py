import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Fix HomeScreen 573
old_canvas_hs = """Canvas(modifier = Modifier
                        .size(340.dp)"""
new_canvas_hs = """val textPrimaryColor = AppColors.TextPrimary
                    Canvas(modifier = Modifier
                        .size(340.dp)"""
content = content.replace(old_canvas_hs, new_canvas_hs)
content = content.replace("color = if (d % 2 == 0) AppColors.TextPrimary else Color(0xFFFFD700),", "color = if (d % 2 == 0) textPrimaryColor else Color(0xFFFFD700),")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/components/PremiumMatchCard.kt", "r") as f:
    p_content = f.read()

old_canvas_pc = "Canvas(modifier = Modifier.matchParentSize()) {"
new_canvas_pc = "val textPrimaryColor = AppColors.TextPrimary\n        Canvas(modifier = Modifier.matchParentSize()) {"
p_content = p_content.replace(old_canvas_pc, new_canvas_pc)
p_content = p_content.replace("AppColors.TextPrimary.copy", "textPrimaryColor.copy")

with open("app/src/main/java/com/example/ui/components/PremiumMatchCard.kt", "w") as f:
    f.write(p_content)

