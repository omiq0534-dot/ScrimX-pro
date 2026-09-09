import re

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import com.example.ui.theme.ThemeManager", import_colors + "import com.example.ui.theme.ThemeManager")

# Update SettingsRow
old_row = """fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDestructive) Color(0xFFFFEBEE) else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = if (isDestructive) Color(0xFFF44336) else Color.Black, 
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 15.sp, 
                color = if (isDestructive) Color(0xFFF44336) else Color.Black
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (badge == "Owner") Color(0xFFFF3366) else Color(0xFFFFD700))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        badge,
                        color = if (badge == "Owner") Color.White else Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.DarkGray)
    }
}"""

new_row = """fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDestructive) Color(0xFFFFEBEE) else AppColors.SubCardBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = if (isDestructive) Color(0xFFF44336) else AppColors.TextPrimary, 
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 15.sp, 
                color = if (isDestructive) Color(0xFFF44336) else AppColors.TextPrimary
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (badge == "Owner") Color(0xFFFF3366) else AppColors.PrimaryAccent)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        badge,
                        color = if (badge == "Owner") Color.White else AppColors.PrimaryAccentText,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AppColors.TextSecondary)
    }
}"""

content = content.replace(old_row, new_row)

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)

print("SettingsRow dynamic color patched")
