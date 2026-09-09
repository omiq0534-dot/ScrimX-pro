import re

def patch_home():
    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
        content = f.read()
    
    # 1. Root background
    content = content.replace('.background(Color(0xFFFAFAFA))', '.background(AppColors.ScreenBackground)')
    
    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
        f.write(content)

def patch_profile():
    with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
        content = f.read()
        
    # TopAppBar colors
    content = content.replace('containerColor = Color(0xFFFAFAFA)', 'containerColor = AppColors.ScreenBackground')
    content = content.replace('scrolledContainerColor = Color(0xFFFAFAFA)', 'scrolledContainerColor = AppColors.ScreenBackground')
    
    # Text colors that were made black for light theme
    content = content.replace('color = Color.Black', 'color = AppColors.TextPrimary')
    
    # The settings menu container
    content = content.replace('.background(AppColors.TextPrimary)', '.background(AppColors.CardBackground)')
    
    # Fix SettingsRow text wrapping by adding weight to the title
    # Currently: Text(title, fontWeight = FontWeight.Bold, ...)
    content = content.replace(
        'Text(\n                title, \n                fontWeight = FontWeight.Bold',
        'Text(\n                title, \n                modifier = Modifier.weight(1f),\n                fontWeight = FontWeight.Bold'
    )
    
    # Also in case it's on one line:
    content = content.replace(
        'Text(title, fontWeight = FontWeight.Bold',
        'Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold'
    )
    
    with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
        f.write(content)

patch_home()
patch_profile()
print("Patched!")
