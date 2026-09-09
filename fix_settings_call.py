import re

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    content = f.read()

old_call = """            SettingsList(
                isOwner = isOwner,
                isModerator = isModerator,
                onAdminClick = {
                    navController.navigate("admin_dashboard")
                },
                onStoreClick = {
                    navController.navigate("store")
                },
                onReferClick = {
                    showReferDialog = true
                },
                onEditProfileClick = {
                    editNameText = profile?.name ?: ""
                    showEditDialog = true
                },
                onSupportClick = {
                    navController.navigate("customer_support")
                },
                onLegalClick = {
                    showLegalDialog = true
                },
                onLogoutClick = {
                    showLogoutDialog = true
                }
            )"""
            
new_call = """            SettingsList(
                isOwner = isOwner,
                isModerator = isModerator,
                isDarkTheme = isDarkTheme,
                onThemeToggleClick = { ThemeManager.toggleTheme(context) },
                onAdminClick = {
                    navController.navigate("admin_dashboard")
                },
                onStoreClick = {
                    navController.navigate("store")
                },
                onReferClick = {
                    showReferDialog = true
                },
                onEditProfileClick = {
                    editNameText = profile?.name ?: ""
                    showEditDialog = true
                },
                onSupportClick = {
                    navController.navigate("customer_support")
                },
                onLegalClick = {
                    showLegalDialog = true
                },
                onLogoutClick = {
                    showLogoutDialog = true
                }
            )"""

content = content.replace(old_call, new_call)

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
    f.write(content)
print("Fixed settings call")
