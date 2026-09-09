import re

with open("app/src/main/java/com/example/ui/screens/WalletScreen.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")

# Scaffold background
content = content.replace('containerColor = Color.Black', 'containerColor = AppColors.ScreenBackground')
content = content.replace('containerColor = Color(0xFF000000)', 'containerColor = AppColors.ScreenBackground')
content = content.replace('containerColor = Color(0xFF090A0F)', 'containerColor = AppColors.ScreenBackground')
content = content.replace('background(Color(0xFF000000))', 'background(AppColors.ScreenBackground)')

# Top bar
content = content.replace('Text("My Wallet", fontWeight = FontWeight.Black, color = Color.White', 'Text("My Wallet", fontWeight = FontWeight.Black, color = AppColors.TextPrimary')
content = content.replace('Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)', 'Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)')

# Balance Cards
content = content.replace('containerColor = Color(0xFF13141B)', 'containerColor = AppColors.CardBackground')
content = content.replace('border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF232533))', 'border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderColor)')
content = content.replace('border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF232533))', 'border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.BorderColor)')

# Texts in Balance Cards
content = content.replace('color = Color.White,', 'color = AppColors.TextPrimary,')
content = content.replace('color = Color.White)', 'color = AppColors.TextPrimary)')
content = content.replace('color = Color(0xFF8B8F9C)', 'color = AppColors.TextSecondary')
content = content.replace('color = Color(0xFFA0A3B5)', 'color = AppColors.TextSecondary')

# Button text
content = content.replace('Text("ADD CASH", color = Color.Black', 'Text("ADD CASH", color = AppColors.PrimaryAccentText')
content = content.replace('Text("WITHDRAW", color = Color.Black', 'Text("WITHDRAW", color = AppColors.PrimaryAccentText')

# Recent Transactions title
content = content.replace('Text("Recent Transactions", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)', 'Text("Recent Transactions", fontWeight = FontWeight.Black, fontSize = 16.sp, color = AppColors.TextPrimary)')

# Transaction item
content = content.replace('.background(Color(0xFF13141B))', '.background(AppColors.CardBackground)')
content = content.replace('border(1.dp, Color(0xFF232533)', 'border(1.dp, AppColors.BorderColor')
content = content.replace('Text(title, fontWeight = FontWeight.Bold, color = Color.White', 'Text(title, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary')

with open("app/src/main/java/com/example/ui/screens/WalletScreen.kt", "w") as f:
    f.write(content)

print("WalletScreen patched.")
