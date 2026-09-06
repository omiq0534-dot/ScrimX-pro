import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# First dialog
content = re.sub(
    r'AlertDialog\(\s*containerColor = Color\(0xFF13151F\),\s*onDismissRequest = \{ if \(!isLoading\) selectedItemForPurchase = null \},',
    r'AlertDialog(\n            containerColor = Color.White,\n            onDismissRequest = { if (!isLoading) selectedItemForPurchase = null },',
    content
)

# Text("Confirm Redemption", color = Color.White
content = re.sub(
    r'Text\("Confirm Redemption", color = Color\.White, fontWeight = FontWeight\.Black, fontSize = 17\.sp\)',
    r'Text("Confirm Redemption", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 17.sp)',
    content
)

# "Are you sure you want to unlock ${item.title}?"
content = re.sub(
    r'color = Color\(0xFFC4C8D8\),\s*fontSize = 13\.sp',
    r'color = Color.DarkGray,\n                        fontSize = 13.sp',
    content
)

# Price Breakdown Box Background
content = re.sub(
    r'\.background\(Color\(0xFF0F111A\)\)\s*\.border\(1\.dp, Color\(0xFF262A38\), RoundedCornerShape\(12\.dp\)\)',
    r'.background(Color(0xFFF3F4F6))\n                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))',
    content
)

content = re.sub(
    r'Text\("Effective Price:", color = Color\(0xFF8E92A4\), fontSize = 12\.sp\)',
    r'Text("Effective Price:", color = Color.Gray, fontSize = 12.sp)',
    content
)
content = re.sub(
    r'Text\("Deduction:", color = Color\(0xFF8E92A4\), fontSize = 12\.sp\)',
    r'Text("Deduction:", color = Color.Gray, fontSize = 12.sp)',
    content
)


# Second dialog
content = re.sub(
    r'AlertDialog\(\s*containerColor = Color\(0xFF13151F\),\s*onDismissRequest = \{ if \(!isLoading\) selectedRechargePlan = null \},',
    r'AlertDialog(\n            containerColor = Color.White,\n            onDismissRequest = { if (!isLoading) selectedRechargePlan = null },',
    content
)

content = re.sub(
    r'Text\("Recharge Confirmation", color = Color\.White',
    r'Text("Recharge Confirmation", color = Color.Black',
    content
)

content = re.sub(
    r'Text\(\s*"Please enter the Mobile Number to recharge:",\s*color = Color\(0xFF8E93A6\)',
    r'Text(\n                        "Please enter the Mobile Number to recharge:",\n                        color = Color.Gray',
    content
)

# Replace OutlinedTextField colors
content = re.sub(
    r'focusedTextColor = Color\.White,\s*unfocusedTextColor = Color\.White,\s*focusedBorderColor = Color\(0xFFFFD700\),\s*unfocusedBorderColor = Color\(0xFF262A38\),\s*cursorColor = Color\(0xFFFFD700\)',
    r'focusedTextColor = Color.Black,\n                            unfocusedTextColor = Color.Black,\n                            focusedBorderColor = Color(0xFFF59E0B),\n                            unfocusedBorderColor = Color.LightGray,\n                            cursorColor = Color.Black',
    content
)


with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
