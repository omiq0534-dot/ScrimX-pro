import re

with open('app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.PlayArrow" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Info", "import androidx.compose.material.icons.filled.Info\nimport androidx.compose.material.icons.filled.PlayArrow")

insertion_point = "            Spacer(modifier = Modifier.height(32.dp))\n            \n            Text(\"Select Slot\","

new_content = """            if (match.status == "Live" || match.status == "Ongoing") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        // Demo action, can open intent to YT
                        android.widget.Toast.makeText(context, "Opening YouTube Live...", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Watch Live")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WATCH LIVE ON YOUTUBE", fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Select Slot","""

content = content.replace(insertion_point, new_content)

with open('app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt', 'w') as f:
    f.write(content)

print("MatchDetailsScreen patched successfully!")
