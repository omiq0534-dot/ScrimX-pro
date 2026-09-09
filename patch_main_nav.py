import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")

# Update AppBottomNav
old_box = """    Box(
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
            .padding(2.dp) // border thickness
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
    ) {"""

new_box = """    Box(
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
            .padding(1.dp) // border thickness
            .clip(RoundedCornerShape(26.dp))
            .background(AppColors.CardBackground)
    ) {"""
content = content.replace(old_box, new_box)

# Now, NavigationBarItemDefaults.colors need dynamic variables
content = content.replace(
    'selectedIconColor = Color.Black,',
    'selectedIconColor = AppColors.ButtonContent,'
)
content = content.replace(
    'selectedTextColor = Color.Black,',
    'selectedTextColor = AppColors.TextPrimary,'
)
content = content.replace(
    'indicatorColor = Color(0xFFF5F5F5),',
    'indicatorColor = AppColors.ButtonContainer,'
)
content = content.replace(
    'unselectedIconColor = Color.Gray,',
    'unselectedIconColor = AppColors.TextSecondary,'
)
content = content.replace(
    'unselectedTextColor = Color.Gray',
    'unselectedTextColor = AppColors.TextSecondary'
)

# Replace the inner sweepBrush colors
# Wait, this brush is for the animated border glow. Let's adapt it to use AppColors.PrimaryAccent.
content = content.replace(
    'Color.Black.copy(alpha = 0.1f)',
    'AppColors.TextPrimary.copy(alpha = 0.1f)'
)
content = content.replace(
    'Color.Black.copy(alpha = 0.8f)',
    'AppColors.PrimaryAccent'
)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)

print("MainScreen bottom nav patched.")
