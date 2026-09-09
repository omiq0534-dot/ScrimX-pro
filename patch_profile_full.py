import re

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

# Make the root Column of ProfileScreen dynamic
root_column_old = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(top = 40.dp)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {"""

root_column_new = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
            .padding(top = 40.dp)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {"""
content = content.replace(root_column_old, root_column_new)

# Profile Name text
content = content.replace(
    'Text(profile?.name ?: "Player", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)',
    'Text(profile?.name ?: "Player", fontSize = 28.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary)'
)

# Email text
content = content.replace(
    'Text(profile?.email ?: "loading...", fontSize = 14.sp, color = Color(0xFFA0AEC0))',
    'Text(profile?.email ?: "loading...", fontSize = 14.sp, color = AppColors.TextSecondary)'
)

# UID text
content = content.replace(
    'Text("UID: ${profile?.uid}", fontSize = 11.sp, color = Color(0xFF718096), fontWeight = FontWeight.SemiBold)',
    'Text("UID: ${profile?.uid}", fontSize = 11.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.SemiBold)'
)

# Replace remaining SettingsList card background logic
# Already handled previously partially, but let's replace `if (isDarkTheme) Color(0xFF131316) else Color.White` with `AppColors.CardBackground`
content = content.replace('if (isDarkTheme) Color(0xFF131316) else Color.White', 'AppColors.CardBackground')
content = content.replace('if (isDarkTheme) Color(0xFF26262D) else Color(0xFFE5E7EB)', 'AppColors.BorderColor')
content = content.replace('if (isDarkTheme) Color(0xFF1D1D23) else Color(0xFFF3F4F6)', 'AppColors.Divider')

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)

print("ProfileScreen full patch complete")
