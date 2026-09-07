with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

# Replace the white backgrounds in DetailBox and Timing box and general cards
content = content.replace('Color(0xFFF7F7FA)', 'Color(0xFF131316)')
content = content.replace('Color(0xFF0B0F14)', 'Color(0xFF08080A)')

# Check DetailBox
detail_box_old = """@Composable
fun DetailBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(16.dp))
            .background(Color(0xFF131316))
            .padding(16.dp)
    ) {
        Text(title.uppercase(), fontSize = 10.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}"""

detail_box_new = """@Composable
fun DetailBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF131316))
            .border(1.2.dp, Color(0xFF26262D), RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 9.5.sp,
            color = Color(0xFF8E8E98),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}"""

if "fun DetailBox" in content:
    import re
    content = re.sub(
        r"@Composable\s+fun DetailBox\(.*?\)\s*\{[\s\S]*?^\}",
        detail_box_new,
        content,
        flags=re.MULTILINE
    )

# Also fix the Timing Box icon tint from Color.Black to Color.White
content = content.replace(
    "Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))",
    "Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))"
)

# And check the slot list and dialogs
with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("Updated MatchDetailsScreen.kt successfully!")
