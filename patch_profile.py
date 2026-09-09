import re

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

# Make the settings list card dynamic
old_settings_list_card = """    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {"""

new_settings_list_card = """    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDarkTheme) Color(0xFF131316) else Color.White)
            .border(1.dp, if (isDarkTheme) Color(0xFF26262D) else Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {"""

content = content.replace(old_settings_list_card, new_settings_list_card)

# Divider
old_divider = "        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))"
new_divider = "        HorizontalDivider(color = if (isDarkTheme) Color(0xFF1D1D23) else Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))"
content = content.replace(old_divider, new_divider)

# Now, we also need to change SettingsRow signature to accept isDarkTheme, but that means passing it everywhere.
# Or, instead of doing it piece by piece via Python, let's just create a centralized DynamicColor object in Theme.kt and use it!
