import re

with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'r') as f:
    content = f.read()

# Introduce showLogoutDialog state
pattern_screen = r'@Composable\s*fun ProfileScreen.*?LazyColumn\('
replacement_screen = """@Composable
fun ProfileScreen(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    val profile by userViewModel.profile.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                Button(
                    onClick = { 
                        showLogoutDialog = false
                        userViewModel.logout()
                        navController.navigate("login") { 
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Log Out", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {"""

# If there's an existing showEditDialog check, just replace it and add showLogoutDialog alongside it
if "var showEditDialog" in content:
    content = re.sub(r'var showEditDialog.*?\n', '', content)
    content = re.sub(r'var editNameText.*?\n', '', content)

content = re.sub(r'@Composable\s*fun ProfileScreen.*?if \(showEditDialog\) \{', replacement_screen, content, flags=re.DOTALL)

# Update the onLogoutClick lambda
content = content.replace(
"""                onLogoutClick = {
                    userViewModel.logout()
                    navController.navigate("login") { 
                        popUpTo("home") { inclusive = true }
                    }
                }""",
"""                onLogoutClick = {
                    showLogoutDialog = true
                }""")


with open('app/src/main/java/com/example/ui/screens/ProfileScreen.kt', 'w') as f:
    f.write(content)

print("ProfileScreen patched successfully!")
