import re

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

# 1. Update ProfileScreen signature/body to get isDarkTheme
import_theme = "import com.example.ui.theme.ThemeManager\nimport androidx.compose.material.icons.filled.DarkMode\nimport androidx.compose.material.icons.filled.LightMode\n"
if "import com.example.ui.theme.ThemeManager" not in content:
    content = content.replace("import androidx.compose.ui.platform.LocalContext", import_theme + "import androidx.compose.ui.platform.LocalContext")

is_dark_val = "    val isDarkTheme by ThemeManager.isDarkTheme.collectAsState()\n"
content = content.replace("    val profile by userViewModel.profile.collectAsState()", is_dark_val + "    val profile by userViewModel.profile.collectAsState()")

# 2. Update SettingsList call in ProfileScreen
old_settings_list_call = """                        SettingsList(
                            isOwner = isOwner,
                            isModerator = isModerator,
                            onAdminClick = { navController.navigate("admin_dashboard") },
                            onStoreClick = { navController.navigate("store") },
                            onReferClick = { showReferDialog = true },
                            onEditProfileClick = { showEditDialog = true },
                            onSupportClick = { navController.navigate("customer_support") },
                            onLegalClick = { showLegalDialog = true },
                            onLogoutClick = { showLogoutDialog = true }
                        )"""
new_settings_list_call = """                        SettingsList(
                            isOwner = isOwner,
                            isModerator = isModerator,
                            isDarkTheme = isDarkTheme,
                            onThemeToggleClick = { ThemeManager.toggleTheme(context) },
                            onAdminClick = { navController.navigate("admin_dashboard") },
                            onStoreClick = { navController.navigate("store") },
                            onReferClick = { showReferDialog = true },
                            onEditProfileClick = { showEditDialog = true },
                            onSupportClick = { navController.navigate("customer_support") },
                            onLegalClick = { showLegalDialog = true },
                            onLogoutClick = { showLogoutDialog = true }
                        )"""
content = content.replace(old_settings_list_call, new_settings_list_call)

# 3. Update SettingsList definition
old_def = """fun SettingsList(
    isOwner: Boolean,
    isModerator: Boolean,
    onAdminClick: () -> Unit,
    onStoreClick: () -> Unit,
    onReferClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSupportClick: () -> Unit,
    onLegalClick: () -> Unit,
    onLogoutClick: () -> Unit
) {"""
new_def = """fun SettingsList(
    isOwner: Boolean,
    isModerator: Boolean,
    isDarkTheme: Boolean,
    onThemeToggleClick: () -> Unit,
    onAdminClick: () -> Unit,
    onStoreClick: () -> Unit,
    onReferClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSupportClick: () -> Unit,
    onLegalClick: () -> Unit,
    onLogoutClick: () -> Unit
) {"""
content = content.replace(old_def, new_def)

# 4. Insert Theme Toggle Row
theme_row = """        SettingsRow(icon = Icons.Default.CardGiftcard, title = "Refer & Earn", badge = "+50 🪙", onClick = onReferClick)
        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))
        SettingsRow(
            icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            title = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
            badge = "New ✨",
            onClick = onThemeToggleClick
        )
        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))"""
content = content.replace("        SettingsRow(icon = Icons.Default.CardGiftcard, title = \"Refer & Earn\", badge = \"+50 🪙\", onClick = onReferClick)\n        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))", theme_row)

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)

print("Patched ProfileScreen.kt successfully!")
