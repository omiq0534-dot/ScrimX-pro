import re

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

# Fix Scaffold TopAppBar texts
content = content.replace('title = { Text("Match Details", fontWeight = FontWeight.Black, color = AppColors.TextPrimary) }', 'title = { Text("Match Details", fontWeight = FontWeight.Black, color = Color.Black) }')
content = content.replace('Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)', 'Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)')

# Fix Main Title
content = content.replace('Text(match.title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary, lineHeight = 30.sp)', 'Text(match.title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black, lineHeight = 30.sp)')

# Fix "SELECT YOUR SLOT" text
content = content.replace('color = AppColors.TextPrimary, \n                    letterSpacing = 0.5.sp\n                )\n                Text("${bookedSlots.size}/$totalSlotsCount Booked"', 'color = Color.Black, \n                    letterSpacing = 0.5.sp\n                )\n                Text("${bookedSlots.size}/$totalSlotsCount Booked"')

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("Fixed MatchDetailsScreen part 1")
