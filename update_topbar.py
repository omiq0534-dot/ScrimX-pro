import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Update TopAppBar Texts and Icons
content = re.sub(
    r'("GAMING REWARDS STORE",\s*fontWeight = FontWeight\.Black,\s*fontSize = 17\.sp,\s*color = )Color\.White,',
    r'\1Color.Black,',
    content
)

content = content.replace('Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)', 
                          'Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)')

# The coin pill in the top bar
content = content.replace('background(Color(0xFF171924))', 'background(Color.White)')
content = content.replace('Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp', 'Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp') # Coin text

# Also TopAppBar containerColor
content = content.replace('containerColor = Color(0xFF0B0C10)', 'containerColor = Color.White')

# "Your Vault is Empty"
content = content.replace('Text("Your Vault is Empty", color = Color.White', 'Text("Your Vault is Empty", color = Color.Black')

# "My Unlocked Cards & Codes"
content = content.replace('Text("My Unlocked Cards & Codes", color = Color.White', 'Text("My Unlocked Cards & Codes", color = Color.Black')

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

