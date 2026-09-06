import re
with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Replace userName.uppercase(), color = Color.White,
content = re.sub(
    r'userName\.uppercase\(\),\s*color = Color\.White,',
    r'userName.uppercase(),\n                            color = Color.Black,',
    content
)

# And "LIMIT $dailyLimit/DAY", color = if (customCodesCount > 0) Color(0xFF00E676) else Color(0xFF6F8299) -> Color.Gray
# The "IN STOCK" / "LIMIT" badge text:
content = re.sub(
    r'color = if \(customCodesCount > 0\) Color\(0xFF00E676\) else Color\(0xFF6F8299\)',
    r'color = if (customCodesCount > 0) Color(0xFF22C55E) else Color.Gray',
    content
)

# And the badge background:
# .background(Color(0xFF14161F))
# .border(0.5.dp, Color(0xFF262A38), RoundedCornerShape(6.dp))
bad_badge = r'\.background\(Color\(0xFF14161F\)\)\s*\.border\(0\.5\.dp, Color\(0xFF262A38\), RoundedCornerShape\(6\.dp\)\)'
good_badge = r'.background(Color(0xFFF3F4F6))\n                            .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))'
content = re.sub(bad_badge, good_badge, content)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
