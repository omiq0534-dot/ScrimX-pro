import re
with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Replace Proof Dialog text color
content = re.sub(
    r'color = Color\(0xFFB0BEC5\)',
    r'color = Color.DarkGray',
    content
)

# Ref ID color
content = re.sub(
    r'color = Color\(0xFFFACC15\)',
    r'color = Color.DarkGray',
    content
)

# Proof dialog inner box
content = re.sub(
    r'\.background\(Color\(0xFF0A0C14\)\)',
    r'.background(Color(0xFFE5E7EB))',
    content
)

# TARGET NUMBER / REDEEM CODE text
content = re.sub(
    r'color = Color\(0xFF6F8299\)',
    r'color = Color.Gray',
    content
)

# PENDING ADMIN APPROVAL
content = re.sub(
    r'color = if \(card\.code\.isNotBlank\(\)\) \(if \(isUsed\) Color\(0xFF8E92A4\) else Color\(0xFFFACC15\)\) else Color\.Gray',
    r'color = if (card.code.isNotBlank()) (if (isUsed) Color.Gray else Color.Black) else Color.Gray',
    content
)
# Note: I replaced the fallback with Color.Gray in the previous step, so it matches.
# Wait, let's just do a simpler replace for the pending admin approval line
content = re.sub(
    r'color = if \(card\.code\.isNotBlank\(\)\) \(if \(isUsed\) Color\(0xFF8E92A4\) else Color\(0xFFFACC15\)\) else Color\(0xFF6F8299\)',
    r'color = if (card.code.isNotBlank()) (if (isUsed) Color.Gray else Color.Black) else Color.Gray',
    content
)
# Just in case the previous replacement already changed 0xFF6F8299 to Color.Gray
content = re.sub(
    r'color = if \(card\.code\.isNotBlank\(\)\) \(if \(isUsed\) Color\(0xFF8E92A4\) else Color\.DarkGray\) else Color\.Gray',
    r'color = if (card.code.isNotBlank()) (if (isUsed) Color.Gray else Color.Black) else Color.Gray',
    content
)

# Copy icon tint
content = re.sub(
    r'tint = if \(isCodeCopied\) Color\(0xFF22C55E\) else Color\(0xFF6F8299\)',
    r'tint = if (isCodeCopied) Color(0xFF22C55E) else Color.Gray',
    content
)
# Just in case the previous replacement already changed 0xFF6F8299 to Color.Gray
content = re.sub(
    r'tint = if \(isCodeCopied\) Color\(0xFF22C55E\) else Color\.Gray',
    r'tint = if (isCodeCopied) Color(0xFF22C55E) else Color.Gray',
    content
)


# View proof button color is fine (green)
# But maybe change status badge used color
content = re.sub(
    r'if \(isUsed\) Color\(0xFF1E2338\) else Color\(0xFF22C55E\)\.copy\(alpha = 0\.15f\)',
    r'if (isUsed) Color.LightGray else Color(0xFF22C55E).copy(alpha = 0.15f)',
    content
)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

