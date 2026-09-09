import re

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

import_colors = "import com.example.ui.theme.AppColors\n"
if "import com.example.ui.theme.AppColors" not in content:
    content = content.replace("import androidx.compose.ui.graphics.Color", import_colors + "import androidx.compose.ui.graphics.Color")

# Replace Scaffold Backgrounds
content = content.replace('containerColor = Color(0xFF08080A)', 'containerColor = AppColors.ScreenBackground')
content = content.replace('background(Color(0xFF0B0F14))', 'background(AppColors.ScreenBackground)')
content = content.replace('background(Color(0xFF08080A))', 'background(AppColors.ScreenBackground)')
content = content.replace('containerColor = Color(0xFF0B0F14)', 'containerColor = AppColors.ScreenBackground')

# TopAppBar titles and background
content = content.replace(
    'title = { Text("Match Details", fontWeight = FontWeight.Black, color = Color.White) }',
    'title = { Text("Match Details", fontWeight = FontWeight.Black, color = AppColors.TextPrimary) }'
)
content = content.replace(
    'Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)',
    'Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)'
)

# Text match.title
content = content.replace(
    'Text(match.title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White',
    'Text(match.title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary'
)

# DetailBox colors
old_detail_box = """fun DetailBox(title: String, value: String, modifier: Modifier = Modifier) {
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
new_detail_box = """fun DetailBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.CardBackground)
            .border(1.2.dp, AppColors.BorderColor, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 9.5.sp,
            color = AppColors.TextSecondary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = AppColors.TextPrimary
        )
    }
}"""
content = content.replace(old_detail_box, new_detail_box)

# Time Box
content = content.replace(
    '.background(Color(0xFF131316))\n                    .border(1.dp, Color(0xFF262A38), RoundedCornerShape(18.dp))',
    '.background(AppColors.CardBackground)\n                    .border(1.dp, AppColors.BorderColor, RoundedCornerShape(18.dp))'
)
content = content.replace(
    'Text("Match Timing: ${match.time}", fontWeight = FontWeight.Bold, color = Color.White',
    'Text("Match Timing: ${match.time}", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary'
)
content = content.replace(
    'Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White',
    'Icon(Icons.Default.AccessTime, contentDescription = null, tint = AppColors.TextPrimary'
)

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("MatchDetailsScreen dynamic colors patched.")
