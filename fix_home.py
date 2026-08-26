import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

pattern_match_btn = r'val isLive = status == "Live" \|\| status == "Ongoing".*?Text\("Join • \$entry", fontWeight = FontWeight\.Bold, fontSize = 14\.sp\)\n\s*\}\n\s*\}'
replacement_match_btn = """val isLive = status == "Live" || status == "Ongoing"
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black, 
                    contentColor = Color.White
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            ) {
                if (isLive) {
                    Text("View / IDP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                } else if (status == "Completed") {
                    Text("View Results", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                } else {
                    Text("Join • $entry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }"""
content = re.sub(pattern_match_btn, replacement_match_btn, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

print("Fixed HomeScreen buttons")
