import re

def revert_home():
    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
        content = f.read()
    
    # 1. Root background
    content = content.replace('.background(AppColors.ScreenBackground)', '.background(Color(0xFFFAFAFA))')
    
    with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
        f.write(content)

def revert_profile():
    with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
        content = f.read()
        
    # TopAppBar colors
    content = content.replace('containerColor = AppColors.ScreenBackground', 'containerColor = Color(0xFFFAFAFA)')
    content = content.replace('scrolledContainerColor = AppColors.ScreenBackground', 'scrolledContainerColor = Color(0xFFFAFAFA)')
    
    # Text colors that were made black for light theme
    content = content.replace('color = AppColors.TextPrimary', 'color = Color.Black')
    
    # The settings menu container
    # Wait, earlier I replaced '.background(AppColors.TextPrimary)' with '.background(AppColors.CardBackground)' 
    # But in light theme it was `.background(AppColors.TextPrimary)` where TextPrimary was White. Wait! 
    # TextPrimary might have been White, but I replaced `Color.Black` with `AppColors.TextPrimary`. 
    # Actually, in the screenshot, the Profile screen has a white background (FAFAFA) and black texts, 
    # and the card background is white (which might be TextPrimary). 
    
    with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "w") as f:
        f.write(content)

revert_home()
revert_profile()
print("Reverted!")
