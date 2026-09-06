import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# 1. FamPayGooglePlayCard -> Remove outer black background
content = re.sub(
    r'\.background\(Color\(0xFF0C0E17\)\)\s*\.border\([^)]+\)',
    r'.background(Color.Transparent)',
    content
)

# 2. PurchasedCardVaultItem changes
# Replace outer column background Color(0xFF0B0F14) with Color(0xFFF3F4F6)
content = re.sub(
    r'\.background\(Color\(0xFF0B0F14\)\)',
    r'.background(Color(0xFFF3F4F6))',
    content
)
# Border around Vault card
content = re.sub(
    r'\.border\(1\.dp, glowColor, RoundedCornerShape\(18\.dp\)\)',
    r'.border(1.dp, Color.LightGray, RoundedCornerShape(18.dp))',
    content
)

# Vault Card Title Color
content = re.sub(
    r'card\.title\.uppercase\(\),\s*color = Color\.White,',
    r'card.title.uppercase(),\n                        color = Color.Black,',
    content
)

# Vault Card Code Box Background
content = re.sub(
    r'\.background\(Color\(0xFF131922\)\)\s*\.border\(1\.dp, Color\(0xFF1E2338\), RoundedCornerShape\(12\.dp\)\)',
    r'.background(Color.White)\n                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))',
    content
)

# Vault Proof Dialog Background
content = re.sub(
    r'\.background\(Color\(0xFF14161F\)\)\s*\.border\(1\.5\.dp, Color\(0xFF22C55E\)\.copy\(alpha = 0\.5f\), RoundedCornerShape\(20\.dp\)\)',
    r'.background(Color.White)\n                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))',
    content
)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
